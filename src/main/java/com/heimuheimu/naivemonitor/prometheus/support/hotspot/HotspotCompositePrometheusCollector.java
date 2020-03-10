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

import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;

import java.util.ArrayList;
import java.util.List;

/**
 * JVM 信息复合采集器，该采集器将会收集以下采集器的信息：
 * <ul>
 *     <li>{@link ClassLoadingPrometheusCollector} JVM 类加载信息采集器</li>
 *     <li>{@link GarbageCollectorPrometheusCollector} JVM GC（垃圾回收）信息采集器</li>
 *     <li>{@link MemoryPrometheusCollector} JVM 内存使用信息采集器</li>
 *     <li>{@link ThreadPrometheusCollector} JVM 线程信息采集器</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class HotspotCompositePrometheusCollector implements PrometheusCollector {

    /**
     * JVM 类加载信息采集器
     */
    private final ClassLoadingPrometheusCollector classLoadingCollector;

    /**
     * JVM GC（垃圾回收）信息采集器
     */
    private final GarbageCollectorPrometheusCollector garbageCollectorCollector;

    /**
     * JVM 内存使用信息采集器
     */
    private final MemoryPrometheusCollector memoryCollector;

    /**
     * JVM 线程信息采集器
     */
    private final ThreadPrometheusCollector threadCollector;

    /**
     * 构造一个 HotspotCompositePrometheusCollector 实例。
     */
    public HotspotCompositePrometheusCollector() {
        this.classLoadingCollector = new ClassLoadingPrometheusCollector();
        this.garbageCollectorCollector = new GarbageCollectorPrometheusCollector();
        this.memoryCollector = new MemoryPrometheusCollector();
        this.threadCollector = new ThreadPrometheusCollector();
    }

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.addAll(classLoadingCollector.getList());
        dataList.addAll(garbageCollectorCollector.getList());
        dataList.addAll(memoryCollector.getList());
        dataList.addAll(threadCollector.getList());
        return dataList;
    }
}
