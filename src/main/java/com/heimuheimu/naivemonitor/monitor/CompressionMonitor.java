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
 * 压缩信息监控器
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class CompressionMonitor {

    /**
     * 已执行的压缩操作次数
     */
    private final AtomicLong compressedCount = new AtomicLong();

    /**
     * 通过压缩操作节约的字节总数
     */
    private final AtomicLong reduceByteCount = new AtomicLong();

    /**
     * 对单次压缩操作进行监控
     *
     * @param byteCount 本次压缩操作节约的字节总数
     */
    public void onCompressed(long byteCount) {
        MonitorUtil.safeAdd(compressedCount, 1);
        MonitorUtil.safeAdd(reduceByteCount, byteCount);
    }

    /**
     * 获得已执行的压缩操作次数
     *
     * @return 已执行的压缩操作次数
     */
    public long getCompressedCount() {
        return compressedCount.get();
    }

    /**
     * 获得通过压缩操作节约的字节总数
     *
     * @return 通过压缩操作节约的字节总数
     */
    public long getReduceByteCount() {
        return reduceByteCount.get();
    }

    @Override
    public String toString() {
        return "CompressionMonitor{" +
                "compressedCount=" + compressedCount +
                ", reduceByteCount=" + reduceByteCount +
                '}';
    }
}
