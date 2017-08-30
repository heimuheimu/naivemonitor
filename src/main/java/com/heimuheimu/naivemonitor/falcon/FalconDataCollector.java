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

package com.heimuheimu.naivemonitor.falcon;

import java.util.List;

/**
 * Falcon 监控数据采集器，Falcon 监控数据上报服务将会根据收集器设置的时间周期进行周期性的采集上报
 * <br><b>注意：采集器采集的 Falcon 监控数据项中的 endpoint 不需要进行设置，将会由上报服务在上报前统一进行设置</b>
 *
 * @author heimuheimu
 */
public interface FalconDataCollector {

    /**
     * 获取该收集器执行的时间周期，单位：秒
     *
     * @return 收集器执行的时间周期，单位：秒
     */
    int getPeriod();

    /**
     * 获取监控数据列表
     * <br><b>注意：采集器采集的 Falcon 监控数据项中的 endpoint 不需要进行设置，将会由上报服务在上报前统一进行设置</b>
     *
     * @return 监控数据列表
     */
    List<FalconData> getList();

}
