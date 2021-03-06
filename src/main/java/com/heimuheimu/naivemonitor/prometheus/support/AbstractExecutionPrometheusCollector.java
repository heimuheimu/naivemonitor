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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 操作执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>${metricPrefix}_exec_count 相邻两次采集周期内操作执行次数</li>
 *     <li>${metricPrefix}_exec_peak_tps_count 相邻两次采集周期内每秒最大执行次数</li>
 *     <li>${metricPrefix}_avg_exec_time_millisecond 相邻两次采集周期内单次操作平均执行时间，单位：毫秒</li>
 *     <li>${metricPrefix}_max_exec_time_millisecond 相邻两次采集周期内单次操作最大执行时间，单位：毫秒</li>
 *     <li>${metricPrefix}_exec_error_count{errorCode="$errorCode",errorType="$errorType"} 相邻两次采集周期内特定类型操作失败次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public abstract class AbstractExecutionPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    protected final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 获得操作执行相关信息 Metric 前缀。
     *
     * @return Metric 前缀
     */
    protected abstract String getMetricPrefix();

    /**
     * 获得操作执行错误类型 Map，Key 为操作失败错误码，Value 为该错误码对应的错误类型，允许返回 {@code null} 或空。
     *
     * <p><strong>注意：</strong>不同的错误代码对应的错误类型应保证唯一。</p>
     *
     * @return 操作执行错误类型 Map，Key 为操作失败错误码，Value 为该错误码对应的错误类型，可能为 {@code null} 或空
     */
    protected abstract Map<Integer, String> getErrorTypeMap();

    /**
     * 获得当前采集器使用的操作执行信息监控器列表，如果返回 {@code null} 或空，调用 {@link #getList()} 方法将会返回空列表。
     *
     * @return 操作执行信息监控器列表
     */
    protected abstract List<ExecutionMonitor> getMonitorList();

    /**
     * 获得 ExecutionMonitor 对应的 ID，每个 ExecutionMonitor 对应的 ID 应保证唯一。
     *
     * @param monitor 操作执行信息监控器
     * @param index 监控器索引
     * @return ExecutionMonitor 对应的 ID
     */
    protected abstract String getMonitorId(ExecutionMonitor monitor, int index);

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
        List<ExecutionMonitor> monitorList = getMonitorList();
        if (monitorList == null || monitorList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, String> errorTypeMap = getErrorTypeMap();
        String metricPrefix = getMetricPrefix();
        PrometheusData executionCountData = PrometheusData.buildGauge(metricPrefix + "_exec_count", "");
        PrometheusData peakTpsCountData = PrometheusData.buildGauge(metricPrefix + "_exec_peak_tps_count", "");
        PrometheusData averageExecutionTimeMillisecondData = PrometheusData.buildGauge(metricPrefix + "_avg_exec_time_millisecond", "");
        PrometheusData maxExecutionTimeMillisecondData = PrometheusData.buildGauge(metricPrefix + "_max_exec_time_millisecond", "");
        PrometheusData errorCountData = PrometheusData.buildGauge(metricPrefix + "_exec_error_count", "");
        for (int i = 0; i < monitorList.size(); i++) {
            ExecutionMonitor monitor = monitorList.get(i);
            String monitorId = getMonitorId(monitor, i);
            // add ${metricPrefix}_exec_count sample
            double executionCount = deltaCalculator.delta("executionCount_" + monitorId, monitor.getTotalCount());
            PrometheusSample executionCountSample = PrometheusSample.build(executionCount);
            executionCountData.addSample(executionCountSample);
            afterAddSample(i, executionCountData, executionCountSample);

            // add ${metricPrefix}_exec_peak_tps_count sample
            PrometheusSample peakTpsSample = PrometheusSample.build(monitor.getPeakTps());
            monitor.resetPeakTps();
            peakTpsCountData.addSample(peakTpsSample);
            afterAddSample(i, peakTpsCountData, peakTpsSample);

            // add ${metricPrefix}_avg_exec_time_millisecond sample
            double averageExecutionTimeMillisecond = 0d;
            if (executionCount > 0) {
                double executionTimeNanosecond = deltaCalculator.delta("executionTime_" + monitorId, monitor.getTotalExecutionTime());
                averageExecutionTimeMillisecond = executionTimeNanosecond / executionCount / 1000000d;
            }
            PrometheusSample averageExecutionTimeMillisecondSample = PrometheusSample.build(averageExecutionTimeMillisecond);
            averageExecutionTimeMillisecondData.addSample(averageExecutionTimeMillisecondSample);
            afterAddSample(i, averageExecutionTimeMillisecondData, averageExecutionTimeMillisecondSample);

            // add ${metricPrefix}_max_exec_time_millisecond sample
            double maxExecutionTimeMillisecond = 0d;
            if (executionCount > 0) {
                maxExecutionTimeMillisecond =  monitor.getMaxExecutionTime() / 1000000d;
                monitor.resetMaxExecutionTime();
            }
            PrometheusSample maxExecutionTimeMillisecondSample = PrometheusSample.build(maxExecutionTimeMillisecond);
            maxExecutionTimeMillisecondData.addSample(maxExecutionTimeMillisecondSample);
            afterAddSample(i, maxExecutionTimeMillisecondData, maxExecutionTimeMillisecondSample);

            // add ${metricPrefix}_exec_error_count
            if (errorTypeMap != null) {
                for (Integer errorCode : errorTypeMap.keySet()) {
                    String errorType = errorTypeMap.get(errorCode);
                    double errorCount = deltaCalculator.delta(errorCode + "_" + monitorId, monitor.getErrorCount(errorCode));
                    PrometheusSample errorCountSample = PrometheusSample.build(errorCount)
                            .addSampleLabel("errorCode", String.valueOf(errorCode))
                            .addSampleLabel("errorType", errorType);
                    errorCountData.addSample(errorCountSample);
                    afterAddSample(i, errorCountData, errorCountSample);
                }
            }
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(executionCountData);
        dataList.add(peakTpsCountData);
        dataList.add(averageExecutionTimeMillisecondData);
        dataList.add(maxExecutionTimeMillisecondData);
        dataList.add(errorCountData);
        return dataList;
    }
}
