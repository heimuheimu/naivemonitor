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

import com.heimuheimu.naivemonitor.monitor.hotspot.gc.GCStatistics;
import com.heimuheimu.naivemonitor.monitor.hotspot.gc.GarbageCollectorMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM GC（垃圾回收）信息采集器，采集时会返回以下数据项：
 * <ul>
 *     <li>hotspot_gc_count{name="$collectorName"} 相邻两次采集周期内执行的 GC 操作次数</li>
 *     <li>hotspot_gc_time_milliseconds{name="$collectorName"} 相邻两次采集周期内执行 GC 操作消耗的总时间，单位：毫秒</li>
 *     <li>hotspot_gc_max_duration_millisecond{name="$collectorName"} 相邻两次采集周期内单次 GC 执行最大时间，单位：毫秒，
 *     <strong>此数据项在部分 JVM 中可能无法统计</strong></li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class GarbageCollectorPrometheusCollector implements PrometheusCollector {

    /**
     * JVM GC（垃圾回收）监控器
     */
    private final GarbageCollectorMonitor monitor = GarbageCollectorMonitor.getInstance();

    /**
     * 上一次采集的 GC 操作统计信息 Map，Key 为垃圾回收器名称，Value 为该回收器上一次采集的 GC 操作统计信息
     */
    private final ConcurrentHashMap<String, GCStatistics> lastGCStatisticsMap = new ConcurrentHashMap<>();

    @Override
    public List<PrometheusData> getList() {
        PrometheusData gcCountData = PrometheusData.buildGauge("hotspot_gc_count", "");
        PrometheusData gcTimeData = PrometheusData.buildGauge("hotspot_gc_time_milliseconds", "");
        PrometheusData gcMaxDurationData = PrometheusData.buildGauge("hotspot_gc_max_duration_millisecond", "");
        for (GCStatistics statistics : monitor.getGCStatisticsList()) {
            GCStatistics lastStatistics = lastGCStatisticsMap.get(statistics.getName());
            // create hotspot_gc_count
            long lastGCCount = lastStatistics != null ? lastStatistics.getCollectionCount() : 0;
            gcCountData.addSample(PrometheusSample.build(statistics.getCollectionCount() - lastGCCount)
                    .addSampleLabel("name", statistics.getName()));

            // create hotspot_gc_time
            long lastGCTime = lastStatistics != null ? lastStatistics.getCollectionTime() : 0;
            gcTimeData.addSample(PrometheusSample.build(statistics.getCollectionTime() - lastGCTime)
                    .addSampleLabel("name", statistics.getName()));

            // create hotspot_gc_max_duration
            if (statistics.getMaxDuration() != GCStatistics.UNKNOWN_MAX_DURATION) {
                gcMaxDurationData.addSample(PrometheusSample.build(statistics.getMaxDuration())
                        .addSampleLabel("name", statistics.getName()));
            }
            lastGCStatisticsMap.put(statistics.getName(), statistics);
        }
        List<PrometheusData> dataList = new ArrayList<>();
        if (!gcCountData.getSampleList().isEmpty()) {
            dataList.add(gcCountData);
        }
        if (!gcTimeData.getSampleList().isEmpty()) {
            dataList.add(gcTimeData);
        }
        if (!gcMaxDurationData.getSampleList().isEmpty()) {
            dataList.add(gcMaxDurationData);
        }
        return dataList;
    }
}
