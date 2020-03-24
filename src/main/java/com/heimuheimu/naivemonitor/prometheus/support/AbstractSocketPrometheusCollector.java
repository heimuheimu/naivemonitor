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

import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Socket 读、写信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>${metricPrefix}_socket_read_count{remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 读取的次数</li>
 *     <li>${metricPrefix}_socket_read_bytes{remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 读取的字节总数</li>
 *     <li>${metricPrefix}_socket_max_read_bytes{remoteAddress="$remoteAddress"} 相邻两次采集周期内单次 Socket 读取的最大字节数</li>
 *     <li>${metricPrefix}_socket_write_count{remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 写入的次数</li>
 *     <li>${metricPrefix}_socket_write_bytes{remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 写入的字节总数</li>
 *     <li>${metricPrefix}_socket_max_write_bytes{remoteAddress="$remoteAddress"} 相邻两次采集周期内单次 Socket 写入的最大字节数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public abstract class AbstractSocketPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    protected final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 获得 Socket 读、写信息 Metric 前缀。
     *
     * @return Metric 前缀
     */
    protected abstract String getMetricPrefix();

    /**
     * 获得当前采集器使用的 Socket 读、写信息监控器列表，如果返回 {@code null} 或空，调用 {@link #getList()} 方法将会返回空列表。
     *
     * @return Socket 读、写信息监控器列表
     */
    protected abstract List<SocketMonitor> getMonitorList();

    /**
     * 获得 SocketMonitor 对应的 ID，每个 SocketMonitor 对应的 ID 应保证唯一。
     *
     * @param monitor Socket 读、写信息监控器
     * @param index 监控器索引
     * @return SocketMonitor 对应的 ID
     */
    protected abstract String getMonitorId(SocketMonitor monitor, int index);

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
        List<SocketMonitor> monitorList = getMonitorList();
        if (monitorList == null || monitorList.isEmpty()) {
            return new ArrayList<>();
        }
        String metricPrefix = getMetricPrefix();
        PrometheusData readCountData = PrometheusData.buildGauge(metricPrefix + "_socket_read_count", "");
        PrometheusData readBytesData = PrometheusData.buildGauge(metricPrefix + "_socket_read_bytes", "");
        PrometheusData maxReadBytesData = PrometheusData.buildGauge(metricPrefix + "_socket_max_read_bytes", "");
        PrometheusData writeCountData = PrometheusData.buildGauge(metricPrefix + "_socket_write_count", "");
        PrometheusData writeBytesData = PrometheusData.buildGauge(metricPrefix + "_socket_write_bytes", "");
        PrometheusData maxWriteBytesData = PrometheusData.buildGauge(metricPrefix + "_socket_max_write_bytes", "");
        for (int i = 0; i < monitorList.size(); i++) {
            SocketMonitor monitor = monitorList.get(i);
            String monitorId = getMonitorId(monitor, i);
            // add ${metricPrefix}_socket_read_count sample
            PrometheusSample readCountSample = PrometheusSample.build(deltaCalculator.delta("readCount_" + monitorId, monitor.getReadCount()))
                    .addSampleLabel("remoteAddress", monitor.getHost());
            readCountData.addSample(readCountSample);
            afterAddSample(i, readCountData, readCountSample);

            // add ${metricPrefix}_socket_read_bytes sample
            PrometheusSample readBytesSample = PrometheusSample.build(deltaCalculator.delta("readBytes_" + monitorId, monitor.getReadByteCount()))
                    .addSampleLabel("remoteAddress", monitor.getHost());
            readBytesData.addSample(readBytesSample);
            afterAddSample(i, readBytesData, readBytesSample);

            // add ${metricPrefix}_socket_max_read_bytes sample
            PrometheusSample maxReadBytesSample = PrometheusSample.build(monitor.getMaxReadByteCount())
                    .addSampleLabel("remoteAddress", monitor.getHost());
            monitor.resetMaxReadByteCount();
            maxReadBytesData.addSample(maxReadBytesSample);
            afterAddSample(i, maxReadBytesData, maxReadBytesSample);

            // add ${metricPrefix}_socket_write_count sample
            PrometheusSample writeCountSample = PrometheusSample.build(deltaCalculator.delta("writeCount_" + monitorId, monitor.getWrittenCount()))
                    .addSampleLabel("remoteAddress", monitor.getHost());
            writeCountData.addSample(writeCountSample);
            afterAddSample(i, writeCountData, writeCountSample);

            // add ${metricPrefix}_socket_write_bytes sample
            PrometheusSample writeBytesSample = PrometheusSample.build(deltaCalculator.delta("writeBytes_" + monitorId, monitor.getWrittenByteCount()))
                    .addSampleLabel("remoteAddress", monitor.getHost());
            writeBytesData.addSample(writeBytesSample);
            afterAddSample(i, writeBytesData, writeBytesSample);

            // add ${metricPrefix}_socket_max_write_bytes
            PrometheusSample maxWriteBytesSample = PrometheusSample.build(monitor.getMaxWrittenByteCount())
                    .addSampleLabel("remoteAddress", monitor.getHost());
            monitor.resetMaxWrittenByteCount();
            maxWriteBytesData.addSample(maxWriteBytesSample);
            afterAddSample(i, maxWriteBytesData, maxWriteBytesSample);
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(readCountData);
        dataList.add(readBytesData);
        dataList.add(maxReadBytesData);
        dataList.add(writeCountData);
        dataList.add(writeBytesData);
        dataList.add(maxWriteBytesData);
        return dataList;
    }
}