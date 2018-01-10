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
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Socket 读、写信息采集器抽象实现类。该采集器将会返回以下数据项：
 * <ul>
 *     <li>{moduleName}_{collectorName}_socket_read_bytes/module={moduleName} 时间周期内 Socket 读取的总字节数</li>
 *     <li>{moduleName}_{collectorName}_socket_avg_read_bytes/module={moduleName} 时间周期内 Socket 每次读取的平均字节数</li>
 *     <li>{moduleName}_{collectorName}_socket_written_bytes/module={moduleName} 时间周期内 Socket 写入的总字节数</li>
 *     <li>{moduleName}_{collectorName}_socket_avg_written_bytes/module={moduleName} 时间周期内 Socket 每次写入的平均字节数</li>
 * </ul>
 *
 * @see SocketMonitor
 * @author heimuheimu
 */
public abstract class AbstractSocketDataCollector extends AbstractFalconDataCollector {

    private volatile long lastReadCount = 0;

    private volatile long lastReadByteCount = 0;

    private volatile long lastWrittenCount = 0;

    private volatile long lastWrittenByteCount = 0;

    /**
     * 获得当前 Socket 读、写信息采集器所依赖的数据源。
     *
     * @return Socket 读、写信息采集器所依赖的数据源
     */
    protected abstract List<SocketMonitor> getSocketMonitorList();

    @Override
    public List<FalconData> getList() {
        List<SocketMonitor> socketMonitorList = getSocketMonitorList();
        List<FalconData> falconDataList = new ArrayList<>();

        long readCount = 0;
        long readByteCount = 0;

        long writtenCount = 0;
        long writtenByteCount = 0;

        for (SocketMonitor socketMonitor : socketMonitorList) {
            readCount += socketMonitor.getReadCount();
            readByteCount += socketMonitor.getReadByteCount();

            writtenCount += socketMonitor.getWrittenCount();
            writtenByteCount += socketMonitor.getWrittenByteCount();
        }

        falconDataList.add(create("_socket_read_bytes", readByteCount - lastReadByteCount));
        long averageReadBytes = 0;
        if (readCount > lastReadCount) {
            averageReadBytes = (readByteCount - lastReadByteCount) / (readCount - lastReadCount);
        }
        falconDataList.add(create("_socket_avg_read_bytes", averageReadBytes));

        falconDataList.add(create("_socket_written_bytes", writtenByteCount - lastWrittenByteCount));
        long averageWrittenBytes = 0;
        if (writtenCount > lastWrittenCount) {
            averageWrittenBytes = (writtenByteCount - lastWrittenByteCount) / (writtenCount - lastWrittenCount);
        }
        falconDataList.add(create("_socket_avg_written_bytes", averageWrittenBytes));

        lastReadCount = readCount;
        lastReadByteCount = readByteCount;

        lastWrittenCount = writtenCount;
        lastWrittenByteCount = writtenByteCount;

        return falconDataList;
    }
}
