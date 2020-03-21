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


package com.heimuheimu.naivemonitor.prometheus.support;

import com.heimuheimu.naivemonitor.monitor.CompressionMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * 压缩操作信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>${metricPrefix}_compression_count 相邻两次采集周期内已执行的压缩次数</li>
 *     <li>${metricPrefix}_compression_reduce_bytes 相邻两次采集周期内通过压缩节省的字节总数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public abstract class AbstractCompressionPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    protected final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 获得压缩操作相关信息 Metric 前缀。
     *
     * @return Metric 前缀
     */
    protected abstract String getMetricPrefix();

    /**
     * 获得当前采集器使用的压缩操作信息监控器列表，不允许返回 {@code null} 或空。
     *
     * @return 压缩操作信息监控器列表
     */
    protected abstract List<CompressionMonitor> getMonitorList();

    /**
     * 获得 CompressionMonitor 对应的 ID，每个 CompressionMonitor 对应的 ID 应保证唯一。
     *
     * @param monitor 压缩操作信息监控器
     * @param index 监控器索引
     * @return CompressionMonitor 对应的 ID
     */
    protected abstract String getMonitorId(CompressionMonitor monitor, int index);

    /**
     * 当添加完成一个样本数据后，将会调用此方法进行回调，通常用于给样本数据添加 Label。
     *
     * @param monitorIndex 监控器索引
     * @param data Prometheus 监控指标
     * @param sample Prometheus 监控指标样本数据
     */
    protected abstract void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample);

    @Override
    public List<PrometheusData> getList() {
        List<CompressionMonitor> monitorList = getMonitorList();
        if (monitorList == null || monitorList.isEmpty()) {
            throw new IllegalArgumentException("Fails to collector prometheus data: `monitorList could not be null or empty`.");
        }
        String metricPrefix = getMetricPrefix();
        PrometheusData compressedCountData = PrometheusData.buildGauge(metricPrefix + "_compression_count", "");
        PrometheusData reduceBytesData = PrometheusData.buildGauge(metricPrefix + "_compression_reduce_bytes", "");
        for (int i = 0; i < monitorList.size(); i++) {
            CompressionMonitor monitor = monitorList.get(i);
            String monitorId = getMonitorId(monitor, i);
            // add ${metricPrefix}_compression_count sample
            double compressedCount = deltaCalculator.delta("compressedCount_" + monitorId, monitor.getCompressedCount());
            PrometheusSample compressedCountSample = PrometheusSample.build(compressedCount);
            compressedCountData.addSample(compressedCountSample);
            afterAddSample(i, compressedCountData, compressedCountSample);

            // add ${metricPrefix}_compression_reduce_bytes sample
            double reduceBytes = deltaCalculator.delta("reduceBytes_" + monitorId, monitor.getReduceByteCount());
            PrometheusSample reduceBytesSample = PrometheusSample.build(reduceBytes);
            reduceBytesData.addSample(reduceBytesSample);
            afterAddSample(i, reduceBytesData, reduceBytesSample);
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(compressedCountData);
        dataList.add(reduceBytesData);
        return dataList;
    }
}
