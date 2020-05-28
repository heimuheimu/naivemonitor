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

package com.heimuheimu.naivemonitor.prometheus.support.hotspot;

import com.heimuheimu.naivemonitor.monitor.hotspot.thread.ThreadMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;

import java.util.ArrayList;
import java.util.List;

/**
 * JVM 线程信息采集器，采集时会返回以下数据项：
 * <ul>
 *     <li>hotspot_thread_count 当前存活线程总数，包括 daemon 和 non-daemon 线程</li>
 *     <li>hotspot_daemon_thread_count 当前存活的 daemon 线程总数</li>
 *     <li>hotspot_total_started_thread_count 累计启动过的线程总数</li>
 *     <li>hotspot_peak_thread_count 相邻两次采集周期内峰值存活线程总数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class ThreadPrometheusCollector implements PrometheusCollector {

    /**
     * JVM 线程信息监控器
     */
    private final ThreadMonitor monitor = ThreadMonitor.getInstance();

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(
                PrometheusData.buildGauge("hotspot_thread_count", "")
                        .addSample(PrometheusSample.build(monitor.getThreadCount()))
        );
        dataList.add(
                PrometheusData.buildGauge("hotspot_daemon_thread_count", "")
                        .addSample(PrometheusSample.build(monitor.getDaemonThreadCount()))
        );
        dataList.add(
                PrometheusData.buildCounter("hotspot_total_started_thread_count", "")
                        .addSample(PrometheusSample.build(monitor.getTotalStartedThreadCount()))
        );
        dataList.add(
                PrometheusData.buildGauge("hotspot_peak_thread_count", "")
                        .addSample(PrometheusSample.build(monitor.getPeakThreadCount()))
        );
        return dataList;
    }
}
