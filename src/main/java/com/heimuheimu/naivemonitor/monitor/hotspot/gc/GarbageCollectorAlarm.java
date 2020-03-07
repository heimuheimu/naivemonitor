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

import com.heimuheimu.naivemonitor.alarm.NaiveServiceAlarm;
import com.heimuheimu.naivemonitor.alarm.ServiceAlarmMessageNotifier;
import com.heimuheimu.naivemonitor.alarm.ServiceContext;
import com.heimuheimu.naivemonitor.util.MonitorUtil;
import com.sun.management.GarbageCollectionNotificationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM GC（垃圾回收）报警器，当 GC 操作时间大于预设值时，将会进行实时报警。
 *
 * <p><strong>说明：</strong>GarbageCollectorAlarm 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class GarbageCollectorAlarm {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageCollectorAlarm.class);

    /**
     * 当前 JVM 运行的项目名称
     */
    private final String project;

    /**
     * 报警器检测的垃圾回收器名称，如果为 {@code null} 或空，则对所有的垃圾回收器都进行监控
     */
    private final String collectorName;

    /**
     * 单次 GC 操作执行时间报警阈值（包含），单位：毫秒，不允许小于等于 0
     */
    private final long durationThreshold;

    /**
     * 运行 JVM 的主机名称
     */
    private final String host;

    /**
     * GC 操作时间实时报警器
     */
    private final NaiveServiceAlarm alarm;

    /**
     * 单次 GC 操作执行时间过长的垃圾回收器 Map，Key 为垃圾回收器名称，Value 为固定值 {@link Boolean#TRUE}
     */
    private final ConcurrentHashMap<String, Boolean> CRASHED_COLLECTOR_MAP = new ConcurrentHashMap<>();

    /**
     * 构造一个 GarbageCollectorAlarm 实例，对所有的垃圾回收器都进行监控。
     *
     * @param project           当前 JVM 运行的项目名称
     * @param durationThreshold 单次 GC 操作执行时间报警阈值（包含），单位：毫秒，不允许小于等于 0
     * @param notifierList      报警消息通知器列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 durationThreshold 小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空，将会抛出此异常
     */
    public GarbageCollectorAlarm(String project, long durationThreshold,
                                 List<ServiceAlarmMessageNotifier> notifierList) {
        this(project, null, durationThreshold, notifierList, null);
    }

    /**
     * 构造一个 GarbageCollectorAlarm 实例，对所有的垃圾回收器都进行监控。
     *
     * @param project           当前 JVM 运行的项目名称
     * @param durationThreshold 单次 GC 操作执行时间报警阈值（包含），单位：毫秒，不允许小于等于 0
     * @param notifierList      报警消息通知器列表，不允许为 {@code null} 或空
     * @param hostAliasMap      别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果 durationThreshold 小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空，将会抛出此异常
     */
    public GarbageCollectorAlarm(String project, long durationThreshold,
                                 List<ServiceAlarmMessageNotifier> notifierList,
                                 Map<String, String> hostAliasMap) {
        this(project, null, durationThreshold, notifierList, hostAliasMap);
    }

    /**
     * 构造一个 GarbageCollectorAlarm 实例。
     *
     * @param project           当前 JVM 运行的项目名称
     * @param collectorName     警器检测的垃圾回收器名称，如果为 {@code null} 或空，则对所有的垃圾回收器都进行监控
     * @param durationThreshold 单次 GC 操作执行时间报警阈值（包含），单位：毫秒，不允许小于等于 0
     * @param notifierList      报警消息通知器列表，不允许为 {@code null} 或空
     * @param hostAliasMap      别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果 durationThreshold 小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空，将会抛出此异常
     */
    public GarbageCollectorAlarm(String project, String collectorName, long durationThreshold,
                                 List<ServiceAlarmMessageNotifier> notifierList,
                                 Map<String, String> hostAliasMap) throws IllegalArgumentException {
        this.project = project;
        this.collectorName = collectorName == null ? "" : collectorName.trim();
        if (durationThreshold <= 0) {
            String errorMessage = "Fails to construct GarbageCollectorAlarm: `durationThreshold could not be less or equal than 0`. `collectorName`:`"
                    + collectorName + "`. `durationThreshold`:`" + durationThreshold + "`. `notifierList`:`" + notifierList + "`";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.durationThreshold = durationThreshold;
        if (notifierList == null || notifierList.isEmpty()) {
            String errorMessage = "Fails to construct GarbageCollectorAlarm: `notifierList could not be empty`. `collectorName`:`"
                    + collectorName + "`. `durationThreshold`:`" + durationThreshold + "`. `notifierList`:`" + notifierList + "`";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.alarm = new NaiveServiceAlarm(notifierList);
        String host = MonitorUtil.getLocalHostName();
        if (hostAliasMap != null && hostAliasMap.containsKey(host)) {
            this.host = hostAliasMap.get(host);
        } else {
            this.host = host;
        }
        GarbageCollectorMonitor.getInstance().registerGarbageCollectorAlarm(this);
    }

    /**
     * 检查 GC 操作时间是否大于等于设置的阈值，如果是，则通过报警消息通知器进行报警。
     *
     * @param gcNotificationInfo GC 操作完成后的通知信息
     */
    public void check(GarbageCollectionNotificationInfo gcNotificationInfo) {
        try {
            String collectorName = gcNotificationInfo.getGcName();
            long duration = gcNotificationInfo.getGcInfo().getDuration();
            if (this.collectorName.isEmpty() || this.collectorName.equals(collectorName)) {
                if (duration >= durationThreshold) {
                    CRASHED_COLLECTOR_MAP.put(collectorName, Boolean.TRUE);
                    alarm.onCrashed(getServiceContext(collectorName));
                    LOGGER.warn("GC duration is too slow. {}", GarbageCollectionNotificationInfoFormatter.format(gcNotificationInfo));
                } else {
                    if (CRASHED_COLLECTOR_MAP.remove(collectorName) != null) {
                        alarm.onRecovered(getServiceContext(collectorName));
                    }
                }
            }
        } catch (Exception e) { //should not happen
            String errorMessage = "Fails to check gc duration: `unexpected error`. " + GarbageCollectionNotificationInfoFormatter.format(gcNotificationInfo);
            LOGGER.error(errorMessage, e);
        }
    }

    private ServiceContext getServiceContext(String collectorName) {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setName("[" + collectorName + "] GC is too slow");
        serviceContext.setHost(host);
        serviceContext.setProject(project);
        return serviceContext;
    }

    @Override
    public String toString() {
        return "GarbageCollectorAlarm{" +
                "project='" + project + '\'' +
                ", collectorName='" + collectorName + '\'' +
                ", durationThreshold=" + durationThreshold +
                ", host='" + host + '\'' +
                '}';
    }
}
