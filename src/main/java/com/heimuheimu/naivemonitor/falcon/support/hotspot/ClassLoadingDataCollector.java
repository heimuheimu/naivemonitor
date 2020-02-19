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

package com.heimuheimu.naivemonitor.falcon.support.hotspot;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.monitor.hotspot.classloading.ClassLoadingMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * JVM 类加载信息采集器。该采集器的采集周期为 30 秒，采集时会返回以下数据项：
 * <ul>
 * <li>hotspot_loaded_class_count/module=hotspot 当前已加载的类数量</li>
 * <li>hotspot_total_loaded_class_count/module=hotspot 累计加载过的类数量</li>
 * <li>hotspot_total_unloaded_class_count/module=hotspot 累计卸载过的类数量</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class ClassLoadingDataCollector extends AbstractHotspotDataCollector {

    /**
     * JVM 类加载信息监控器
     */
    private final ClassLoadingMonitor monitor = ClassLoadingMonitor.getInstance();

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();
        // create hotspot_loaded_class_count
        FalconData data = create("_loaded_class_count", monitor.getLoadedClassCount());
        falconDataList.add(data);

        // create hotspot_total_loaded_class_count
        data = create("_total_loaded_class_count", monitor.getTotalLoadedClassCount());
        falconDataList.add(data);

        // create hotspot_total_unloaded_class_count
        data = create("_total_unloaded_class_count", monitor.getTotalUnloadedClassCount());
        falconDataList.add(data);
        return falconDataList;
    }
}
