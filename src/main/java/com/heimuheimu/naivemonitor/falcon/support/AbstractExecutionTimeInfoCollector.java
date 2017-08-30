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

import com.heimuheimu.naivemonitor.ExecutionTimeInfo;
import com.heimuheimu.naivemonitor.falcon.FalconData;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行时间信息采集器抽象实现
 *
 * @author heimuheimu
 */
public abstract class AbstractExecutionTimeInfoCollector extends AbstractFalconDataCollector {

    private volatile long lastExecutionCount = 0;

    private volatile long lastTotalExecutionTime = 0;

    /**
     * 获得当前执行时间信息采集器所依赖的数据源
     *
     * @return 执行时间信息采集器所依赖的数据源
     */
    protected abstract ExecutionTimeInfo getExecutionTimeInfo();

    @Override
    public List<FalconData> getList() {
        ExecutionTimeInfo executionTimeInfo = getExecutionTimeInfo();
        List<FalconData> falconDataList = new ArrayList<>();

        //平均执行时间
        long executionCount = executionTimeInfo.getCount();
        long totalExecutionTime = executionTimeInfo.getTotalExecutionTime();
        double avgExecTime;
        if (executionCount > lastExecutionCount) {
            avgExecTime = (totalExecutionTime - lastTotalExecutionTime) / (executionCount - lastExecutionCount);
        } else {
            avgExecTime = 0;
        }
        falconDataList.add(create("_avg_exec_time", avgExecTime));
        lastExecutionCount = executionCount;
        lastTotalExecutionTime = totalExecutionTime;

        //最大执行时间
        falconDataList.add(create("_max_exec_time", executionTimeInfo.getMaxExecutionTime()));
        executionTimeInfo.resetMaxExecutionTime();

        return falconDataList;
    }
}
