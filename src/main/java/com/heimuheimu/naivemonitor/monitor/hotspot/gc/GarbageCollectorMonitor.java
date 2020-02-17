/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naivemonitor.monitor.hotspot.gc;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JVM GC（垃圾回收）监控器，可根据名称获取该回收器已执行的 GC 次数、总执行时间、单次最大执行时间等信息。
 *
 * <p><strong>说明：</strong>GarbageCollectorMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class GarbageCollectorMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageCollectorMonitor.class);

    /**
     * GarbageCollectorMonitor 是否支持单次 GC 执行最大时间统计
     */
    private static final boolean isGarbageCollectionNotificationInfoSupported;

    static {
        boolean isClassExist = false;
        try {
            Class.forName("com.sun.management.GarbageCollectionNotificationInfo");
            isClassExist = true;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Fails to collect GC max collections time: `com.sun.management.GarbageCollectionNotificationInfo is not exist`.");
        }
        isGarbageCollectionNotificationInfoSupported = isClassExist;
    }

    /**
     * {@link #getInstance()} 方法返回的 GarbageCollectorMonitor 实例，访问此变量需获得锁 "GarbageCollectorMonitor.class"
     */
    private static GarbageCollectorMonitor GC_MONITOR = null;


    /**
     * 单次 GC 执行最大时间 Map，Key 为垃圾回收器名称，Value 为该垃圾回收器对应的单次 GC 执行最大时间，单位：毫秒
     */
    private final ConcurrentHashMap<String, Long> MAX_DURATION_MAP = new ConcurrentHashMap<>();

    /**
     * JVM GC（垃圾回收）报警器列表，当 GC 操作时间大于预设值时，将会进行实时报警
     */
    private final CopyOnWriteArrayList<GarbageCollectorAlarm> GC_ALARM_LIST = new CopyOnWriteArrayList<>();

    /**
     * 构造一个 GarbageCollectorMonitor 实例。
     */
    private GarbageCollectorMonitor() {
        if (isGarbageCollectionNotificationInfoSupported) {
            GarbageCollectorNotificationListener gcNotificationListener = new GarbageCollectorNotificationListener();
            for (GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (gcMXBean instanceof NotificationEmitter) {
                    ((NotificationEmitter) gcMXBean).addNotificationListener(gcNotificationListener, null, null);
                    LOGGER.info("Add gc notification success, name: `" + gcMXBean.getName() + "`.");
                }
            }
        }
    }

    /**
     * 采集并返回 JVM GC（垃圾回收）操作统计信息列表，该方法不会返回 {@code null}。
     *
     * <p><strong>注意：</strong>本方法执行后会重置已统计的垃圾回收器单次 GC 执行最大时间。</p>
     *
     * @return JVM GC（垃圾回收）操作统计信息列表，不会为 {@code null}
     */
    public List<GCStatistics> getGCStatisticsList() {
        List<GCStatistics> statisticsList = new ArrayList<>();
        try {
            for (GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                String collectorName = gcMXBean.getName();
                GCStatistics statistics = new GCStatistics();
                statistics.setName(collectorName);
                statistics.setCollectionCount(gcMXBean.getCollectionCount());
                statistics.setCollectionTime(gcMXBean.getCollectionTime());
                if (isGarbageCollectionNotificationInfoSupported) {
                    Long maxDuration = MAX_DURATION_MAP.remove(collectorName);
                    statistics.setMaxDuration(maxDuration != null ? maxDuration : 0L);
                } else {
                    statistics.setMaxDuration(GCStatistics.UNKNOWN_MAX_DURATION);
                }
                statisticsList.add(statistics);
            }
        } catch (Exception e) { //should not happen
            LOGGER.error("Fails to get gc statistics list: `unexpected error`.", e);
        }
        return statisticsList;
    }

    /**
     * 将 GarbageCollectorAlarm 注册到当前 GarbageCollectorMonitor 实例中。
     *
     * @param gcAlarm JVM GC（垃圾回收）报警器
     */
    void registerGarbageCollectorAlarm(GarbageCollectorAlarm gcAlarm) {
        GC_ALARM_LIST.add(gcAlarm);
        LOGGER.info("Register GarbageCollectorAlarm success: {}", gcAlarm);
    }

    /**
     * 获得 GarbageCollectorMonitor 实例，用于监控 JVM GC（垃圾回收）操作。
     *
     * @return GarbageCollectorMonitor 实例
     */
    public static synchronized GarbageCollectorMonitor getInstance() {
        if (GC_MONITOR == null) {
            GC_MONITOR = new GarbageCollectorMonitor();
        }
        return GC_MONITOR;
    }

    /**
     * JVM GC（垃圾回收）事件监听器，该监听器依赖与 JVM 实现，需要使用 "com.sun.management" 包。
     *
     * <p><strong>说明：</strong>GarbageCollectorNotificationListener 类是线程安全的，可在多个线程中使用同一个实例。</p>
     *
     * @see GarbageCollectionNotificationInfo
     */
    private class GarbageCollectorNotificationListener implements NotificationListener {

        @Override
        public void handleNotification(Notification notification, Object handback) {
            try {
                if (GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType())) {
                    CompositeData compositeData = (CompositeData) notification.getUserData();
                    GarbageCollectionNotificationInfo gcNotificationInfo = GarbageCollectionNotificationInfo.from(compositeData);
                    GcInfo gcInfo = gcNotificationInfo.getGcInfo();
                    String collectorName = gcNotificationInfo.getGcName();
                    long duration = gcInfo.getDuration();
                    updateMaxDuration(collectorName, duration);
                    LOGGER.debug("Update max collections time success. `gcName`:`{}`. `duration`:`{}ms`.",
                            collectorName, duration);
                    for (GarbageCollectorAlarm gcAlarm : GC_ALARM_LIST) {
                        try {
                            gcAlarm.check(collectorName, duration);
                        } catch (Exception e) { //should not happen
                            LOGGER.error("Fails to check gc duration: `unexpected error`. `collectorName`:`"
                                    + collectorName + "`. `duration`:`" + duration + "ms`. `gcAlarm`:`" + gcAlarm + "`", e);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Fails to handle gc notification: `unexpected error`. " + notification, e);
            }
        }

        /**
         * 更新单次 GC 执行最大时间。
         *
         * @param collectorName 垃圾回收器名称
         * @param duration      本次 GC 执行时间，单位：毫秒
         */
        private void updateMaxDuration(String collectorName, long duration) {
            MAX_DURATION_MAP.compute(collectorName, (theCollectorName, currentMaxDuration) -> {
                if (currentMaxDuration != null) {
                    return duration > currentMaxDuration ? duration : currentMaxDuration;
                } else {
                    return duration;
                }
            });
        }
    }
}
