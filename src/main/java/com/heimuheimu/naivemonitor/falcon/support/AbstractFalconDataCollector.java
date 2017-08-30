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
import com.heimuheimu.naivemonitor.falcon.FalconDataCollector;

/**
 * Falcon 监控数据采集器抽象实现
 *
 * @author heimuheimu
 */
public abstract class AbstractFalconDataCollector implements FalconDataCollector {

    /**
     * 获得当前监控数据所在的模块名称，将会在 {@link FalconData#getMetric()} 中作为前缀出现，也会出现在 {@link FalconData#getTags()} 中
     *
     * @return 当前监控数据所在的模块名称，将会在 {@link FalconData#getMetric()} 中作为前缀出现，也会出现在 {@link FalconData#getTags()} 中
     */
    protected abstract String getModuleName();

    /**
     * 获得当前采集器名称，默认为空字符串，该名称将会作为 {@link FalconData#getMetric()} 中的一部分出现
     *
     * @return 采集器名称
     */
    protected String getCollectorName() {
        return "";
    }

    /**
     * 创建一个 Falcon 监控数据项，endpoint 不进行设置
     *
     * @param metricSuffix Metric 后缀，用于与 {@link #getModuleName()} 组装成完整的 Metric
     * @param value 该 metric 在当前时间点的值
     * @return Falcon 监控数据项
     */
    protected FalconData create(String metricSuffix, double value) {
        FalconData data = new FalconData();
        data.setStep(getPeriod());
        data.setMetric(getMetric(metricSuffix));
        data.setTags("module=" + getModuleName());
        data.setTimestamp(System.currentTimeMillis() / 1000);
        data.setValue(value);
        return data;
    }

    private String getMetric(String metricSuffix) {
        if (getCollectorName().isEmpty()) {
            return getModuleName() + metricSuffix;
        } else {
            return getModuleName() + "_" + getCollectorName() + metricSuffix;
        }
    }

}
