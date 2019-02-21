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

package com.heimuheimu.naivemonitor.monitor;

import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Socket 读、写信息监控器，可提供 Socket 读或写操作的次数、字节总数等信息。
 *
 * <p><strong>说明：</strong>{@code SocketMonitor} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @see com.heimuheimu.naivemonitor.falcon.support.AbstractSocketDataCollector
 * @author heimuheimu
 */
public class SocketMonitor {

    /**
     * Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141，允许为空字符串
     */
    private final String host;

    /**
     * Socket 读操作执行次数
     */
    private final AtomicLong readCount = new AtomicLong();

    /**
     * Socket 读操作累计读取的字节总数
     */
    private final AtomicLong readByteCount = new AtomicLong();

    /**
     * Socket 单次读操作读取的最大字节总数
     */
    private volatile long maxReadByteCount = 0;

    /**
     * Socket 写操作执行次数
     */
    private final AtomicLong writtenCount = new AtomicLong();

    /**
     * Socket 写操作累计写入的字节总数
     */
    private final AtomicLong writtenByteCount = new AtomicLong();

    /**
     * Socket 单次写操作写入的最大字节总数
     */
    private volatile long maxWrittenByteCount = 0;

    /**
     * 构造一个 Socket 读、写信息监控器。
     *
     * @param host Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141，允许为空字符串
     */
    public SocketMonitor(String host) {
        if (host == null) {
            this.host = "";
        } else {
            this.host = host.trim();
        }
    }

    /**
     * 监控 Socket 完成的一次读取操作。
     *
     * @param byteCount 本次 Socket 读取到的字节长度
     */
    public void onRead(long byteCount) {
        MonitorUtil.safeAdd(readCount, 1);
        MonitorUtil.safeAdd(readByteCount, byteCount);
        if (byteCount > maxReadByteCount) {
            maxReadByteCount = byteCount;
        }
    }

    /**
     * 监控 Socket 完成的一次写入操作。
     *
     * @param byteCount 本次 Socket 写入的字节长度
     */
    public void onWritten(long byteCount) {
        MonitorUtil.safeAdd(writtenCount, 1);
        MonitorUtil.safeAdd(writtenByteCount, byteCount);
        if (byteCount > maxWrittenByteCount) {
            maxWrittenByteCount = byteCount;
        }
    }

    /**
     * 重置 Socket 单次读操作读取的最大字节总数。
     */
    public void resetMaxReadByteCount() {
        this.maxReadByteCount = 0;
    }

    /**
     * 重置 Socket 单次写操作写入的最大字节总数。
     */
    public void resetMaxWrittenByteCount() {
       this.maxWrittenByteCount = 0;
    }

    /**
     * 获得 Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141。
     *
     * @return Socket 连接目标地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得 Socket 读操作执行次数。
     *
     * @return Socket 读操作执行次数
     */
    public long getReadCount() {
        return readCount.get();
    }

    /**
     * 获得 Socket 读操作累计读取的字节总数。
     *
     * @return Socket 读操作累计读取的字节总数
     */
    public long getReadByteCount() {
        return readByteCount.get();
    }

    /**
     * 获得 Socket 单次读操作读取的最大字节总数。
     *
     * @return Socket 单次读操作读取的最大字节总数
     */
    public long getMaxReadByteCount() {
        return maxReadByteCount;
    }

    /**
     * 获得 Socket 写操作执行次数。
     *
     * @return Socket 写操作执行次数
     */
    public long getWrittenCount() {
        return writtenCount.get();
    }

    /**
     * 获得 Socket 写操作累计写入的字节总数。
     *
     * @return Socket 写操作累计写入的字节总数
     */
    public long getWrittenByteCount() {
        return writtenByteCount.get();
    }

    /**
     * 获得 Socket 单次写操作写入的最大字节总数。
     *
     * @return Socket 单次写操作写入的最大字节总数
     */
    public long getMaxWrittenByteCount() {
        return maxWrittenByteCount;
    }

    @Override
    public String toString() {
        return "SocketMonitor{" +
                "host='" + host + '\'' +
                ", readCount=" + readCount +
                ", readByteCount=" + readByteCount +
                ", maxReadByteCount=" + maxReadByteCount +
                ", writtenCount=" + writtenCount +
                ", writtenByteCount=" + writtenByteCount +
                ", maxWrittenByteCount=" + maxWrittenByteCount +
                '}';
    }
}
