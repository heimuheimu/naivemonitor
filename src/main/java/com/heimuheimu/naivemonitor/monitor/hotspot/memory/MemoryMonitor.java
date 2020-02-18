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

package com.heimuheimu.naivemonitor.monitor.hotspot.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM 内存使用信息监控器。
 *
 * <p><strong>说明：</strong>MemoryMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MemoryMonitor {

    /**
     * {@link #getInstance()} 方法返回的 MemoryMonitor 实例，访问此变量需获得锁 "MemoryMonitor.class"
     */
    private static MemoryMonitor MEMORY_MONITOR = null;

    /**
     * 内存管理器
     */
    private final MemoryMXBean memoryMXBean;

    /**
     * 内存池管理器列表
     */
    private final List<MemoryPoolMXBean> memoryPoolMXBeanList;

    /**
     * 构造一个 MemoryMonitor 实例。
     */
    private MemoryMonitor() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.memoryPoolMXBeanList = ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * 获得当前 heap 区域内存使用信息，heap 区域由 1 个或多个 type 为 heap 的内存池组成，返回的内存使用信息中的 used 和 committed 的大小
     * 为上述内存池对应各值的总和，而 init 和 max 的大小则反应了 heap 区域的设置，不一定为上述内存池对应各值的总和。
     *
     * @return Heap 区域内存使用信息
     */
    public MemoryUsage getHeapMemoryUsage() {
        return memoryMXBean.getHeapMemoryUsage();
    }

    /**
     * 获得当前 non-heap 区域内存使用信息。non-heap 区域由 1 个或多个 type 为 non-heap 的内存池组成，返回的内存使用信息中的 used
     * 和 committed 的大小为上述内存池对应各值的总和，而 init 和 max 的大小则反应了 non-heap 区域的设置，不一定为上述内存池对应各值的总和。
     *
     * @return non-heap 区域内存使用信息
     */
    public MemoryUsage getNonHeapMemoryUsage() {
        return memoryMXBean.getNonHeapMemoryUsage();
    }

    /**
     * 获得各内存池的当前内存使用信息 Map，Key 为内存池名称，Value 为该内存池当前的内存使用信息。
     *
     * @return 当前内存使用信息 Map，Key 为内存池名称，Value 为该内存池当前的内存使用信息
     */
    public Map<String, MemoryUsage> getMemoryPoolUsageMap() {
        Map<String, MemoryUsage> usageMap = new HashMap<>();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeanList) {
            MemoryUsage usage = memoryPoolMXBean.getUsage();
            if (usage != null) {
                usageMap.put(memoryPoolMXBean.getName(), usage);
            }
        }
        return usageMap;
    }

    /**
     * 获得各内存池的峰值内存使用信息 Map，Key 为内存池名称，Value 为该内存池的内存峰值使用信息。
     *
     * <p><strong>注意：</strong>本方法执行后会重置各内存池的峰值内存使用信息统计。</p>
     *
     * @return 内存池的峰值内存使用信息 Map，Key 为内存池名称，Value 为该内存池的内存峰值使用信息
     */
    public Map<String, MemoryUsage> getPeakMemoryPoolUsageMap() {
        Map<String, MemoryUsage> peakUsageMap = new HashMap<>();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeanList) {
            MemoryUsage peakUsage = memoryPoolMXBean.getPeakUsage();
            if (peakUsage != null) {
                peakUsageMap.put(memoryPoolMXBean.getName(), peakUsage);
                memoryPoolMXBean.resetPeakUsage(); // 重置该内存池的峰值内存使用信息
            }
        }
        return peakUsageMap;
    }

    /**
     * 获得 MemoryMonitor 实例，用于监控 JVM 内存使用信息。
     *
     * @return MemoryMonitor 实例
     */
    public static synchronized MemoryMonitor getInstance() {
        if (MEMORY_MONITOR == null) {
            MEMORY_MONITOR = new MemoryMonitor();
        }
        return MEMORY_MONITOR;
    }
}
