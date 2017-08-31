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

import com.heimuheimu.naivemonitor.MonitorUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Socket 信息监控器
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SocketMonitor {

    /**
     * Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141
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
     * Socket 写操作执行次数
     */
    private final AtomicLong writtenCount = new AtomicLong();

    /**
     * Socket 写操作累计写入的字节总数
     */
    private final AtomicLong writtenByteCount = new AtomicLong();

    /**
     * 构造一个 Socket 信息监控器
     *
     * @param host Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141
     */
    public SocketMonitor(String host) {
        this.host = host;
    }

    /**
     * 监控 Socket 完成的一次读取操作
     *
     * @param byteCount 本次 Socket 读取到的字节长度
     */
    public void onRead(long byteCount) {
        MonitorUtil.safeAdd(readCount, 1);
        MonitorUtil.safeAdd(readByteCount, byteCount);
    }

    /**
     * 监控 Socket 完成的一次写入操作
     *
     * @param byteCount 本次 Socket 写入的字节长度
     */
    public void onWritten(long byteCount) {
        MonitorUtil.safeAdd(writtenCount, 1);
        MonitorUtil.safeAdd(writtenByteCount, byteCount);
    }

    /**
     * 获得 Socket 连接目标地址，通常由主机名和端口组成，":"符号分割，例如：localhost:4141
     *
     * @return Socket 连接目标地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得 Socket 读操作执行次数
     *
     * @return Socket 读操作执行次数
     */
    public long getReadCount() {
        return readCount.get();
    }

    /**
     * 获得 Socket 读操作累计读取的字节总数
     *
     * @return Socket 读操作累计读取的字节总数
     */
    public long getReadByteCount() {
        return readByteCount.get();
    }

    /**
     * 获得 Socket 写操作执行次数
     *
     * @return Socket 写操作执行次数
     */
    public long getWrittenCount() {
        return writtenCount.get();
    }

    /**
     * 获得 Socket 写操作累计写入的字节总数
     *
     * @return Socket 写操作累计写入的字节总数
     */
    public long getWrittenByteCount() {
        return writtenByteCount.get();
    }

    @Override
    public String toString() {
        return "SocketMonitor{" +
                "host='" + host + '\'' +
                ", readCount=" + readCount +
                ", readByteCount=" + readByteCount +
                ", writtenCount=" + writtenCount +
                ", writtenByteCount=" + writtenByteCount +
                '}';
    }
}
