/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 heimuheimu
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

package com.heimuheimu.naivemonitor.falcon.support;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作执行信息采集器抽象实现
 *
 * @author heimuheimu
 */
public abstract class AbstractExecutionInfoCollector extends AbstractFalconDataCollector {

    private volatile long lastExecutionCount = 0;

    private volatile long lastTotalExecutionTime = 0;

    private final ConcurrentHashMap<Integer, Long> lastErrorCountMap = new ConcurrentHashMap<>();

    /**
     * 获得当前操作执行信息采集器所依赖的数据源
     *
     * @return 操作执行信息采集器所依赖的数据源
     */
    protected abstract List<ExecutionMonitor> getExecutionMonitorList();

    /**
     * 获得需要采集的执行错误次数 Map，Key 为错误码， Value 为该错误码对应的 Metric 后缀，例如 "_too_busy"
     *
     * @return 需要采集的执行错误次数 Map
     */
    protected Map<Integer, String> getErrorMetricSuffixMap() {
        return null;
    }

    @Override
    public List<FalconData> getList() {
        List<ExecutionMonitor> executionMonitorList = getExecutionMonitorList();
        Map<Integer, String> errorMetricSuffixMap = getErrorMetricSuffixMap();
        List<FalconData> falconDataList = new ArrayList<>();

        long executionCount = 0;
        long peakTps = 0;
        HashMap<Integer, Long> errorCountMap = new HashMap<>();
        long totalExecutionTime = 0;
        long maxSuccessExecutionTime = 0;
        for (ExecutionMonitor executionMonitor : executionMonitorList) {
            executionCount += executionMonitor.getTotalCount();

            long currentPeakTps = executionMonitor.getPeakTps();
            executionMonitor.resetPeakTps();
            peakTps = peakTps < currentPeakTps ? currentPeakTps : peakTps;

            if (errorMetricSuffixMap != null && !errorMetricSuffixMap.isEmpty()) {
                for (Integer errorCode : errorMetricSuffixMap.keySet()) {
                    Long errorCount = errorCountMap.get(errorCode);
                    if (errorCount == null) {
                        errorCount = 0L;
                    }
                    errorCount += executionMonitor.getErrorCount(errorCode);
                    errorCountMap.put(errorCode, errorCount);
                }
            }

            totalExecutionTime += executionMonitor.getTotalExecutionTime();

            long currentMaxExecutionTime = executionMonitor.getMaxSuccessExecutionTime();
            executionMonitor.resetMaxSuccessExecutionTime();
            maxSuccessExecutionTime = maxSuccessExecutionTime < currentMaxExecutionTime ? currentMaxExecutionTime : maxSuccessExecutionTime;
        }

        falconDataList.add(create("_tps", (executionCount - lastExecutionCount) / getPeriod()));

        falconDataList.add(create("_peak_tps", (executionCount - lastExecutionCount) / getPeriod()));

        if (errorMetricSuffixMap != null && !errorMetricSuffixMap.isEmpty()) {
            for (Integer errorCode : errorMetricSuffixMap.keySet()) {
                Long errorCount =  errorCountMap.get(errorCode);
                if (errorCount != null) {
                    Long lastErrorCount = lastErrorCountMap.get(errorCode);
                    if (lastErrorCount == null) {
                        lastErrorCount = 0L;
                    }
                    falconDataList.add(create(errorMetricSuffixMap.get(errorCode), errorCount - lastErrorCount));
                    lastErrorCountMap.put(errorCode, errorCount);
                }
            }
        }

        double avgExecTime;
        if (executionCount > lastExecutionCount) {
            avgExecTime = (totalExecutionTime - lastTotalExecutionTime) / (executionCount - lastExecutionCount);
        } else {
            avgExecTime = 0;
        }
        falconDataList.add(create("_avg_exec_time", avgExecTime));
        lastExecutionCount = executionCount;
        lastTotalExecutionTime = totalExecutionTime;

        falconDataList.add(create("_max_exec_time", maxSuccessExecutionTime));
        return falconDataList;
    }
}
