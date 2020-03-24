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

import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程池信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>${metricPrefix}_threadPool_reject_count 相邻两次采集周期内监控器中所有线程池拒绝执行的任务总数</li>
 *     <li>${metricPrefix}_threadPool_active_count 采集时刻监控器中的所有线程池活跃线程数近似值总和</li>
 *     <li>${metricPrefix}_threadPool_pool_size 采集时刻监控器中的所有线程池线程数总和</li>
 *     <li>${metricPrefix}_threadPool_peak_pool_size 监控器中的所有线程池出现过的最大线程数总和</li>
 *     <li>${metricPrefix}_threadPool_core_pool_size 监控器中的所有线程池配置的核心线程数总和</li>
 *     <li>${metricPrefix}_threadPool_maximum_pool_size 监控器中的所有线程池配置的最大线程数总和</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public abstract class AbstractThreadPoolPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    protected final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 获得线程池相关信息 Metric 前缀。
     *
     * @return Metric 前缀
     */
    protected abstract String getMetricPrefix();

    /**
     * 获得当前采集器使用的线程池信息监控器列表，如果返回 {@code null} 或空，调用 {@link #getList()} 方法将会返回空列表。
     *
     * @return 线程池信息监控器列表
     */
    protected abstract List<ThreadPoolMonitor> getMonitorList();

    /**
     * 获得 ThreadPoolMonitor 对应的 ID，每个 ThreadPoolMonitor 对应的 ID 应保证唯一。
     *
     * @param monitor 线程池信息监控器
     * @param index 监控器索引
     * @return ThreadPoolMonitor 对应的 ID
     */
    protected abstract String getMonitorId(ThreadPoolMonitor monitor, int index);

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
        List<ThreadPoolMonitor> monitorList = getMonitorList();
        if (monitorList == null || monitorList.isEmpty()) {
            return new ArrayList<>();
        }
        String metricPrefix = getMetricPrefix();
        PrometheusData activeCountData = PrometheusData.buildGauge(metricPrefix + "_threadPool_active_count", "");
        PrometheusData poolSizeData = PrometheusData.buildGauge(metricPrefix + "_threadPool_pool_size", "");
        PrometheusData peakPoolSizeData = PrometheusData.buildGauge(metricPrefix + "_threadPool_peak_pool_size", "");
        PrometheusData corePoolSizeData = PrometheusData.buildGauge(metricPrefix + "_threadPool_core_pool_size", "");
        PrometheusData maximumPoolSizeData = PrometheusData.buildGauge(metricPrefix + "_threadPool_maximum_pool_size", "");
        PrometheusData rejectedCountData = PrometheusData.buildGauge(metricPrefix + "_threadPool_reject_count", "");
        for (int i = 0; i < monitorList.size(); i++) {
            ThreadPoolMonitor monitor = monitorList.get(i);
            String monitorId = getMonitorId(monitor, i);
            // add ${metricPrefix}_threadPool_active_count sample
            PrometheusSample activeCountSample = PrometheusSample.build(monitor.getActiveCount());
            activeCountData.addSample(activeCountSample);
            afterAddSample(i, activeCountData, activeCountSample);

            // add ${metricPrefix}_threadPool_pool_size sample
            PrometheusSample poolSizeSample = PrometheusSample.build(monitor.getPoolSize());
            poolSizeData.addSample(poolSizeSample);
            afterAddSample(i, poolSizeData, poolSizeSample);

            // add ${metricPrefix}_threadPool_peak_pool_size sample
            PrometheusSample peakPoolSizeSample = PrometheusSample.build(monitor.getPeakPoolSize());
            peakPoolSizeData.addSample(peakPoolSizeSample);
            afterAddSample(i, peakPoolSizeData, peakPoolSizeSample);

            // add ${metricPrefix}_threadPool_core_pool_size sample
            PrometheusSample corePoolSizeSample = PrometheusSample.build(monitor.getCorePoolSize());
            corePoolSizeData.addSample(corePoolSizeSample);
            afterAddSample(i, corePoolSizeData, corePoolSizeSample);

            // add ${metricPrefix}_threadPool_maximum_pool_size sample
            PrometheusSample maximumPoolSizeSample = PrometheusSample.build(monitor.getMaximumPoolSize());
            maximumPoolSizeData.addSample(maximumPoolSizeSample);
            afterAddSample(i, maximumPoolSizeData, maximumPoolSizeSample);

            // add ${metricPrefix}_threadPool_reject_count sample
            double rejectCount = deltaCalculator.delta("rejectCount_" + monitorId, monitor.getRejectedCount());
            PrometheusSample rejectedCountSample = PrometheusSample.build(rejectCount);
            rejectedCountData.addSample(rejectedCountSample);
            afterAddSample(i, rejectedCountData, rejectedCountSample);
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(activeCountData);
        dataList.add(poolSizeData);
        dataList.add(peakPoolSizeData);
        dataList.add(corePoolSizeData);
        dataList.add(maximumPoolSizeData);
        dataList.add(rejectedCountData);
        return dataList;
    }
}
