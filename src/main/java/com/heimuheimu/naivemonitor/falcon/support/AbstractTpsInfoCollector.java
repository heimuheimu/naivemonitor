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

import com.heimuheimu.naivemonitor.TpsInfo;
import com.heimuheimu.naivemonitor.falcon.FalconData;

import java.util.ArrayList;
import java.util.List;

/**
 * 每秒事务处理数信息采集器抽象实现
 *
 * @author heimuheimu
 */
public abstract class AbstractTpsInfoCollector extends AbstractFalconDataCollector {

    private volatile long lastTpsCount = 0;

    /**
     * 获得当前每秒事务处理数信息采集器所依赖的数据源
     *
     * @return 每秒事务处理数信息采集器所依赖的数据源
     */
    protected abstract TpsInfo getTpsInfo();

    @Override
    public List<FalconData> getList() {
        TpsInfo tpsInfo = getTpsInfo();
        List<FalconData> falconDataList = new ArrayList<>();

        long tpsCount = tpsInfo.getCount();
        falconDataList.add(create("_tps", (tpsCount - lastTpsCount) / getPeriod()));
        lastTpsCount = tpsCount;

        falconDataList.add(create("_peak_tps", tpsInfo.getPeakTps()));
        tpsInfo.resetPeakTps();

        return falconDataList;
    }
}
