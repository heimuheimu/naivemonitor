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

/**
 * JVM GC（垃圾回收）操作统计信息。
 *
 * <p><strong>说明：</strong>GCStatistics 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class GCStatistics {

    /**
     * 当单次 GC 执行最大时间无法统计时使用的值
     */
    public static final long UNKNOWN_MAX_DURATION = -1L;

    /**
     * 垃圾回收器名称
     */
    private String name;

    /**
     * 已执行的 GC 操作次数
     */
    private long collectionCount;

    /**
     * 已执行的 GC 总执行时间，单位：毫秒
     */
    private long collectionTime;

    /**
     * 单次 GC 执行最大时间，单位：毫秒，如果此项无法统计，该值为 {@link #UNKNOWN_MAX_DURATION}
     */
    private long maxDuration = UNKNOWN_MAX_DURATION;

    /**
     * 获得垃圾回收器名称。
     *
     * @return 垃圾回收器名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置垃圾回收器名称。
     *
     * @param name 垃圾回收器名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得已执行的 GC 操作次数。
     *
     * @return 已执行的 GC 操作次数
     */
    public long getCollectionCount() {
        return collectionCount;
    }

    /**
     * 设置已执行的 GC 操作次数。
     *
     * @param collectionCount 已执行的 GC 操作次数
     */
    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    /**
     * 获得已执行的 GC 总执行时间，单位：毫秒。
     *
     * @return 已执行的 GC 总执行时间，单位：毫秒
     */
    public long getCollectionTime() {
        return collectionTime;
    }

    /**
     * 设置已执行的 GC 总执行时间，单位：毫秒。
     *
     * @param collectionTime 已执行的 GC 总执行时间，单位：毫秒
     */
    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    /**
     * 获得单次 GC 执行最大时间，单位：毫秒，如果此项无法统计，该值为 {@link #UNKNOWN_MAX_DURATION}。
     *
     * @return 单次 GC 执行最大时间，单位：毫秒，如果此项无法统计，该值为 {@link #UNKNOWN_MAX_DURATION}
     */
    public long getMaxDuration() {
        return maxDuration;
    }

    /**
     * 设置单次 GC 执行最大时间，单位：毫秒，如果此项无法统计，该值为 {@link #UNKNOWN_MAX_DURATION}。
     *
     * @param maxDuration 单次 GC 执行最大时间，单位：毫秒，如果此项无法统计，该值为 {@link #UNKNOWN_MAX_DURATION}
     */
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public String toString() {
        return "GCStatistics{" +
                "name='" + name + '\'' +
                ", collectionCount=" + collectionCount +
                ", collectionTime=" + collectionTime +
                ", maxDuration=" + maxDuration +
                '}';
    }
}
