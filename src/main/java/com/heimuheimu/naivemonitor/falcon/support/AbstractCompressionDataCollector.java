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
import com.heimuheimu.naivemonitor.monitor.CompressionMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 压缩操作监控数据采集器抽象实现类。该采集器将会返回以下数据项：
 * <ul>
 *     <li>{moduleName}_{collectorName}_compression_reduce_bytes/module={moduleName} 时间周期内压缩操作已节省的字节数</li>
 *     <li>{moduleName}_{collectorName}_compression_avg_reduce_bytes/module={moduleName} 时间周期内平均每次压缩操作节省的字节数</li>
 * </ul>
 *
 * @see CompressionMonitor
 * @author heimuheimu
 */
public abstract class AbstractCompressionDataCollector extends AbstractFalconDataCollector {

    private volatile long lastCompressedCount = 0;

    private volatile long lastReduceBytes = 0;

    /**
     * 获得压缩操作监控数据采集器所依赖的数据源。
     *
     * @return 压缩信息采集器所依赖的数据源
     */
    protected abstract List<CompressionMonitor> getCompressionMonitorList();

    @Override
    public List<FalconData> getList() {
        List<CompressionMonitor> compressionMonitorList = getCompressionMonitorList();
        List<FalconData> falconDataList = new ArrayList<>();

        long compressedCount = 0;
        long reduceBytes = 0;

        for (CompressionMonitor compressionMonitor : compressionMonitorList) {
            compressedCount += compressionMonitor.getCompressedCount();
            reduceBytes += compressionMonitor.getReduceByteCount();
        }

        falconDataList.add(create("_compression_reduce_bytes", reduceBytes - lastReduceBytes));
        long averageReduceBytes = 0;
        if (compressedCount > lastCompressedCount) {
            averageReduceBytes = (reduceBytes - lastReduceBytes) / (compressedCount - lastCompressedCount);
        }
        falconDataList.add(create("_compression_avg_reduce_bytes", averageReduceBytes));

        lastCompressedCount = compressedCount;
        lastReduceBytes = reduceBytes;

        return falconDataList;
    }
}
