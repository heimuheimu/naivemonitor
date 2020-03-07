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

package com.heimuheimu.naivemonitor.prometheus.support.hotspot;

import com.heimuheimu.naivemonitor.monitor.hotspot.memory.MemoryMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;

import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JVM 内存使用信息采集器，采集时会返回以下数据项：
 * <ul>
 *     <li>hotspot_heap_memory_init_bytes 当前 heap 内存区域初始化内存大小，单位：字节</li>
 *     <li>hotspot_heap_memory_used_bytes 当前 heap 内存区域正在使用的内存大小，单位：字节</li>
 *     <li>hotspot_heap_memory_committed_bytes 当前 heap 内存区域保证可使用的内存大小，单位：字节</li>
 *     <li>hotspot_heap_memory_max_bytes 当前 heap 内存区域最大可使用的内存大小，单位：字节</li>
 *     <li>hotspot_nonheap_memory_init_bytes 当前 non-heap 内存区域初始化内存大小，单位：字节</li>
 *     <li>hotspot_nonheap_memory_used_bytes 当前 non-heap 内存区域正在使用的内存大小，单位：字节</li>
 *     <li>hotspot_nonheap_memory_committed_bytes 当前 non-heap 内存区域保证可使用的内存大小，单位：字节</li>
 *     <li>hotspot_nonheap_memory_max_bytes 当前 non-heap 内存区域最大可使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_init_bytes{name="$poolName"} 该内存池区域初始化内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_used_bytes{name="$poolName"} 该内存池区域正在使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_committed_bytes{name="$poolName"} 该内存池区域保证可使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_max_bytes{name="$poolName"} 该内存池区域最大可使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_peak_init_bytes{name="$poolName"} 相邻两次采集周期内该内存池区域达到使用峰值时的初始化内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_peak_used_bytes{name="$poolName"} 相邻两次采集周期内该内存池区域达到使用峰值时的使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_peak_committed_bytes{name="$poolName"} 相邻两次采集周期内该内存池区域达到使用峰值时的保证可使用的内存大小，单位：字节</li>
 *     <li>hotspot_memory_pool_peak_max_bytes{name="$poolName"} 相邻两次采集周期内该内存池区域达到使用峰值时的最大可使用的内存大小，单位：字节</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class MemoryPrometheusCollector implements PrometheusCollector {

    /**
     * JVM 内存使用信息监控器
     */
    private final MemoryMonitor monitor = MemoryMonitor.getInstance();

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        addHeapMemoryUsage(dataList);
        addNonHeapMemoryUsage(dataList);
        addMemoryPoolUsage("hotspot_memory_pool_", monitor.getMemoryPoolUsageMap(), dataList);
        addMemoryPoolUsage("hotspot_memory_pool_peak_", monitor.getPeakMemoryPoolUsageMap(), dataList);
        return dataList;
    }

    /**
     * 将当前 heap 区域内存使用信息添加到指定的 Prometheus 监控指标列表当中。
     *
     * @param target 目标 Prometheus 监控指标列表
     */
    public void addHeapMemoryUsage(List<PrometheusData> target) {
        MemoryUsage memoryUsage = monitor.getHeapMemoryUsage();
        target.add(
                PrometheusData.buildGauge("hotspot_heap_memory_init_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getInit()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_heap_memory_used_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getUsed()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_heap_memory_committed_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getCommitted()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_heap_memory_max_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getMax()))
        );
    }

    /**
     * 将当前 non-heap 区域内存使用信息添加到指定的 Prometheus 监控指标列表当中。
     *
     * @param target 目标 Prometheus 监控指标列表
     */
    public void addNonHeapMemoryUsage(List<PrometheusData> target) {
        MemoryUsage memoryUsage = monitor.getNonHeapMemoryUsage();
        target.add(
                PrometheusData.buildGauge("hotspot_nonheap_memory_init_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getInit()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_nonheap_memory_used_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getUsed()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_nonheap_memory_committed_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getCommitted()))
        );
        target.add(
                PrometheusData.buildGauge("hotspot_nonheap_memory_max_bytes", "")
                        .addSample(PrometheusSample.build(memoryUsage.getMax()))
        );
    }

    /**
     * 将各内存池的当前内存使用信息添加到指定的 Prometheus 监控指标列表当中。
     *
     * @param target 目标 Prometheus 监控指标列表
     */
    public void addMemoryPoolUsage(String metricPrefix, Map<String, MemoryUsage> memoryPoolUsageMap,
                                   List<PrometheusData> target) {
        if (!memoryPoolUsageMap.isEmpty()) {
            PrometheusData initData = PrometheusData.buildGauge(metricPrefix + "init_bytes", "");
            PrometheusData usedData = PrometheusData.buildGauge(metricPrefix + "used_bytes", "");
            PrometheusData committedData = PrometheusData.buildGauge(metricPrefix + "committed_bytes", "");
            PrometheusData maxData = PrometheusData.buildGauge(metricPrefix + "max_bytes", "");
            for (String poolName : memoryPoolUsageMap.keySet()) {
                MemoryUsage usage = memoryPoolUsageMap.get(poolName);
                initData.addSample(PrometheusSample.build(usage.getInit())
                        .addSampleLabel("name", poolName));
                usedData.addSample(PrometheusSample.build(usage.getUsed())
                        .addSampleLabel("name", poolName));
                committedData.addSample(PrometheusSample.build(usage.getCommitted())
                        .addSampleLabel("name", poolName));
                maxData.addSample(PrometheusSample.build(usage.getMax())
                        .addSampleLabel("name", poolName));
            }
            target.add(initData);
            target.add(usedData);
            target.add(committedData);
            target.add(maxData);
        }
    }
}
