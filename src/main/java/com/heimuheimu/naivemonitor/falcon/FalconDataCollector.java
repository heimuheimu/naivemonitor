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
 * Falcon 监控数据采集器，{@link FalconReporter} 会周期性的调用 {@link #getList()} 方法，将返回的监控数据列表推送至 Falcon 系统。
 *
 * <p><strong>说明：</strong>{@code FalconDataCollector} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface FalconDataCollector {

    /**
     * 获取该采集器执行的时间周期，单位：秒，不建议设置小于 30 的值。
     *
     * @return 采集器执行的时间周期，单位：秒
     */
    int getPeriod();

    /**
     * 获取 Falcon 监控数据列表，由 {@link FalconReporter} 周期性调用并推送至 Falcon 系统。
     *
     * <p><strong>注意：</strong>监控数据中的 Endpoint 由 {@link FalconReporter} 统一设置，采集器中设置的 Endpoint 值将会被覆盖。</p>
     *
     * @return Falcon 监控数据列表
     */
    List<FalconData> getList();

}
