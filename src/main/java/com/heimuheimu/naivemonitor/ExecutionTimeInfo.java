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
 * 执行时间(nanoTime)统计信息
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 * @see System#nanoTime()
 */
public class ExecutionTimeInfo {

    /**
     * 最大执行时间(nanoTime)
     */
    private volatile long maxExecutionTime = 0;

    /**
     * 总执行时间(nanoTime)
     */
    private final AtomicLong totalExecutionTime = new AtomicLong();

    /**
     * 统计次数
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * 增加一个执行时间(nanoTime)统计信息
     *
     * @param startTime 执行开始时间(nanoTime)，使用 {@link System#nanoTime()}获取
     */
    public void add(long startTime) {
        long estimatedTime = System.nanoTime() - startTime;
        count.incrementAndGet();
        totalExecutionTime.addAndGet(estimatedTime);
        //最大执行时间仅使用了 volatile 来保证可见性，并没有保证操作的原子性，极端情况下，真正的最大值可能会被覆盖，但做统计影响不大
        if (estimatedTime > maxExecutionTime) {
            maxExecutionTime = estimatedTime;
        }
    }

    /**
     * 获得平均执行时间(nanoTime)
     *
     * @return 平均执行时间(nanoTime)
     */
    public long getAverageExecutionTime() {
        return totalExecutionTime.get() / count.get();
    }

    /**
     * 获得最大执行时间(nanoTime)
     *
     * @return 最大执行时间(nanoTime)
     */
    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    /**
     * 获得总执行时间(nanoTime)
     *
     * @return 总执行时间(nanoTime)
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    /**
     * 获得统计次数
     *
     * @return 统计次数
     */
    public long getCount() {
        return count.get();
    }

    /**
     * 重置最大执行时间(nanoTime)
     */
    public void resetMaxExecutionTime() {
        maxExecutionTime = 0;
    }

    @Override
    public String toString() {
        return "ExecutionTimeInfo{" +
                "maxExecutionTime=" + maxExecutionTime +
                ", totalExecutionTime=" + totalExecutionTime +
                ", count=" + count +
                '}';
    }

}
