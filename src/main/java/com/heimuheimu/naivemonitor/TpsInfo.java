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

package com.heimuheimu.naivemonitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 记录每秒事务处理数 TPS (Transaction per second) 统计信息
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class TpsInfo {

    /**
     * 当前对象创建时间戳
     */
    private final long createdTime = System.currentTimeMillis();

    /**
     * 总事务数
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * 每秒最大事务处理数 TPS (Transaction per second)
     */
    private volatile long peakTps = 0;

    /**
     * 达到每秒最大事务处理数的开始时间戳
     */
    private volatile long peakTpsTimestamp = 0;

    /**
     * 当前秒事务处理数 TPS (Transaction per second)
     */
    private final AtomicLong currentTps = new AtomicLong();

    /**
     * 当前秒开始时间戳
     */
    private volatile long currentTpsTimestamp = 0;

    /**
     * 私有锁
     */
    private final Object lock = new Object();

    /**
     * 增加一个事务数统计
     */
    public void add() {
        long currentTimestamp = System.currentTimeMillis();
        count.incrementAndGet();
        if (currentTimestamp - currentTpsTimestamp <= 1000) {
            currentTps.incrementAndGet();
        } else {
            synchronized (lock) {
                if (currentTimestamp - currentTpsTimestamp > 1000) {
                    if (currentTps.get() > peakTps) {
                        peakTpsTimestamp = currentTpsTimestamp;
                        peakTps = currentTps.get();
                    }
                    currentTpsTimestamp = currentTimestamp;
                    currentTps.set(0);
                } else {
                    currentTps.incrementAndGet();
                }
            }
        }
    }

    /**
     * 获得当前对象创建时间戳
     *
     * @return 当前对象创建时间戳
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * 获得总事务数
     *
     * @return 总事务数
     */
    public long getCount() {
        return count.get();
    }

    /**
     * 获得每秒最大事务处理数 TPS (Transaction per second)
     *
     * @return 每秒最大事务处理数 TPS (Transaction per second)
     */
    public long getPeakTps() {
        return peakTps;
    }

    /**
     * 获得达到每秒最大事务处理数的开始时间戳
     *
     * @return 达到每秒最大事务处理数的开始时间戳
     */
    public long getPeakTpsTimestamp() {
        return peakTpsTimestamp;
    }

    /**
     * 重置每秒最大事务处理数 TPS (Transaction per second)
     */
    public void resetPeakTps() {
        peakTps = 0;
        peakTpsTimestamp = 0;
    }

    @Override
    public String toString() {
        return "TpsInfo{" +
                "createdTime=" + createdTime +
                ", count=" + count +
                ", peakTps=" + peakTps +
                ", peakTpsTimestamp=" + peakTpsTimestamp +
                ", currentTps=" + currentTps +
                ", currentTpsTimestamp=" + currentTpsTimestamp +
                '}';
    }
}
