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

package com.heimuheimu.naivemonitor.falcon.support.hotspot;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.monitor.hotspot.memory.MemoryMonitor;

import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JVM 内存使用信息采集器。该采集器的采集周期为 30 秒，采集时会返回以下数据项：
 * <ul>
 * <li>hotspot_heap_memory_init_bytes/module=hotspot 当前 heap 内存区域初始化内存大小，单位：字节</li>
 * <li>hotspot_heap_memory_used_bytes/module=hotspot 当前 heap 内存区域正在使用的内存大小，单位：字节</li>
 * <li>hotspot_heap_memory_committed_bytes/module=hotspot 当前 heap 内存区域保证可使用的内存大小，单位：字节</li>
 * <li>hotspot_heap_memory_max_bytes/module=hotspot 当前 heap 内存区域最大可使用的内存大小，单位：字节</li>
 * <li>hotspot_nonheap_memory_init_bytes/module=hotspot 当前 non-heap 内存区域初始化内存大小，单位：字节</li>
 * <li>hotspot_nonheap_memory_used_bytes/module=hotspot 当前 non-heap 内存区域正在使用的内存大小，单位：字节</li>
 * <li>hotspot_nonheap_memory_committed_bytes/module=hotspot 当前 non-heap 内存区域保证可使用的内存大小，单位：字节</li>
 * <li>hotspot_nonheap_memory_max_bytes/module=hotspot 当前 non-heap 内存区域最大可使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_init_bytes/module=hotspot,name={poolName} 该内存池区域初始化内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_used_bytes/module=hotspot,name={poolName} 该内存池区域正在使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_committed_bytes/module=hotspot,name={poolName} 该内存池区域保证可使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_max_bytes/module=hotspot,name={poolName} 该内存池区域最大可使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_peak_init_bytes/module=hotspot,name={poolName} 30 秒内该内存池区域达到使用峰值时的初始化内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_peak_used_bytes/module=hotspot,name={poolName} 30 秒内该内存池区域达到使用峰值时的使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_peak_committed_bytes/module=hotspot,name={poolName} 30 秒内该内存池区域达到使用峰值时的保证可使用的内存大小，单位：字节</li>
 * <li>hotspot_memory_pool_peak_max_bytes/module=hotspot,name={poolName} 30 秒内该内存池区域达到使用峰值时的最大可使用的内存大小，单位：字节</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class MemoryDataCollector extends AbstractHotspotDataCollector {

    /**
     * JVM 内存使用信息监控器
     */
    private final MemoryMonitor monitor = MemoryMonitor.getInstance();

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();
        falconDataList.addAll(buildFalconDataList(monitor.getHeapMemoryUsage(), "_heap_memory", null));
        falconDataList.addAll(buildFalconDataList(monitor.getNonHeapMemoryUsage(), "_nonheap_memory", null));
        Map<String, MemoryUsage> usageMap = monitor.getMemoryPoolUsageMap();
        for (String poolName : usageMap.keySet()) {
            falconDataList.addAll(buildFalconDataList(usageMap.get(poolName), "_memory_pool", poolName));
        }
        Map<String, MemoryUsage> peakUsageMap = monitor.getPeakMemoryPoolUsageMap();
        for (String poolName : peakUsageMap.keySet()) {
            falconDataList.addAll(buildFalconDataList(peakUsageMap.get(poolName), "_memory_pool_peak", poolName));
        }
        return falconDataList;
    }

    /**
     * 根据内存使用信息创建对应的 Falcon 监控数据列表。
     *
     * @param usage        内存使用信息
     * @param metricSuffix 指标 Metric 后缀
     * @param poolName     内存池名称，允许为 {@code null} 或空
     * @return 内存使用信息对应的 Falcon 监控数据列表
     */
    private List<FalconData> buildFalconDataList(MemoryUsage usage, String metricSuffix, String poolName) {
        List<FalconData> falconDataList = new ArrayList<>();
        // create {metricSuffix}_init_bytes
        FalconData data = create(metricSuffix + "_init_bytes", usage.getInit());
        if (poolName != null && !poolName.isEmpty()) {
            data.setTags(data.getTags() + ",name=" + poolName);
        }
        falconDataList.add(data);

        // create {metricSuffix}_used_bytes
        data = create(metricSuffix + "_used_bytes", usage.getUsed());
        if (poolName != null && !poolName.isEmpty()) {
            data.setTags(data.getTags() + ",name=" + poolName);
        }
        falconDataList.add(data);

        // create {metricSuffix}_committed_bytes
        data = create(metricSuffix + "_committed_bytes", usage.getCommitted());
        if (poolName != null && !poolName.isEmpty()) {
            data.setTags(data.getTags() + ",name=" + poolName);
        }
        falconDataList.add(data);

        // create {metricSuffix}_max_bytes
        data = create(metricSuffix + "_max_bytes", usage.getMax());
        if (poolName != null && !poolName.isEmpty()) {
            data.setTags(data.getTags() + ",name=" + poolName);
        }
        falconDataList.add(data);
        return falconDataList;
    }
}
