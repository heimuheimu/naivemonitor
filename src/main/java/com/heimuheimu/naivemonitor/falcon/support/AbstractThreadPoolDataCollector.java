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
import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程池信息采集器抽象实现
 *
 * @author heimuheimu
 */
public abstract class AbstractThreadPoolDataCollector extends AbstractFalconDataCollector {

    private volatile long lastRejectedCount = 0;

    /**
     * 获得当前线程池信息采集器所依赖的数据源
     *
     * @return 线程池信息采集器所依赖的数据源
     */
    protected abstract ThreadPoolMonitor getThreadPoolMonitor();

    @Override
    public List<FalconData> getList() {
        ThreadPoolMonitor threadPoolMonitor = getThreadPoolMonitor();
        List<FalconData> falconDataList = new ArrayList<>();

        falconDataList.add(create("_threadPool_active_count", threadPoolMonitor.getActiveCount()));

        falconDataList.add(create("_threadPool_pool_size", threadPoolMonitor.getPoolSize()));

        falconDataList.add(create("_threadPool_peak_pool_size", threadPoolMonitor.getPeakPoolSize()));

        falconDataList.add(create("_threadPool_core_pool_size", threadPoolMonitor.getCorePoolSize()));

        falconDataList.add(create("_threadPool_maximum_pool_size", threadPoolMonitor.getMaximumPoolSize()));

        long rejectedCount = threadPoolMonitor.getRejectedCount();
        falconDataList.add(create("_threadPool_rejected_count", rejectedCount - lastRejectedCount));
        lastRejectedCount = rejectedCount;

        return falconDataList;
    }

}
