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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 操作执行信息监控器
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class ExecutionMonitor {

    /**
     * 操作执行总次数
     */
    private final AtomicLong totalCount = new AtomicLong();

    /**
     * 操作执行成功总次数
     */
    private final AtomicLong successCount = new AtomicLong();

    /**
     * 操作执行失败总次数 Map，Key 为操作失败错误码，Value 为该错误码对应的失败次数
     */
    private final ConcurrentHashMap<Integer, AtomicLong> errorCountMap = new ConcurrentHashMap<>();

    /**
     * 每秒最大操作执行数
     */
    private volatile long peakTps = 0;

    /**
     * 当前秒操作执行数
     */
    private final AtomicLong currentTps = new AtomicLong();

    /**
     * 当前秒开始时间戳
     */
    private volatile long currentTpsTimestamp = 0;

    /**
     * 操作执行成功的最大执行时间，单位：纳秒
     */
    private volatile long maxSuccessExecutionTime = 0;

    /**
     * 操作总执行时间，单位：纳秒
     */
    private final AtomicLong totalExecutionTime = new AtomicLong();

    /**
     * 对执行成功的操作进行监控，执行开始时间应该在操作开始前执行 {@link System#nanoTime()} 方法获取
     *
     * @param startNanoTime 操作执行开始时间，单位：纳秒
     */
    public void onExecutedSuccess(long startNanoTime) {
        long estimatedTime = System.nanoTime() - startNanoTime;
        onExecuted(estimatedTime);

        //最大执行时间仅使用了 volatile 来保证可见性，并没有保证操作的原子性，极端情况下，真正的最大值可能会被覆盖，但做统计影响不大
        if (estimatedTime > maxSuccessExecutionTime) {
            maxSuccessExecutionTime = estimatedTime;
        }

        //操作执行成功总次数 +1
        MonitorUtil.safeAdd(successCount, 1);
    }

    /**
     * 对执行失败的操作进行监控，执行开始时间应该在操作开始前执行 {@link System#nanoTime()} 方法获取
     *
     * @param startNanoTime 操作执行开始时间，单位：纳秒
     * @param errorCode 操作失败错误码，由使用方自行定义
     */
    public void onExecutedError(long startNanoTime, int errorCode) {
        long estimatedTime = System.nanoTime() - startNanoTime;
        onExecuted(estimatedTime);

        //操作执行失败总次数 +1
        AtomicLong errorCount = errorCountMap.get(errorCode);
        if (errorCount == null) {
            errorCount = new AtomicLong();
            errorCountMap.put(errorCode, errorCount);
        }
        MonitorUtil.safeAdd(errorCount, 1);
    }

    /**
     * 获得操作执行总次数
     *
     * @return 操作执行总次数
     */
    public long getTotalCount() {
        return totalCount.get();
    }

    /**
     * 获得操作执行成功总次数
     *
     * @return 操作执行成功总次数
     */
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * 获得错误码对应的操作执行失败总次数
     *
     * @param errorCode 错误码
     * @return 错误码对应的操作执行失败总次数
     */
    public long getErrorCount(int errorCode) {
        AtomicLong errorCount = errorCountMap.get(errorCode);
        if (errorCount != null) {
            return errorCount.get();
        } else {
            return 0;
        }
    }

    /**
     * 获得每秒最大操作执行数
     *
     * @return 每秒最大操作执行数
     */
    public long getPeakTps() {
        return peakTps;
    }

    /**
     * 获得操作执行成功的最大执行时间，单位：纳秒
     *
     * @return 操作执行成功的最大执行时间，单位：纳秒
     */
    public long getMaxSuccessExecutionTime() {
        return maxSuccessExecutionTime;
    }

    /**
     * 获得操作总执行时间，单位：纳秒
     *
     * @return 操作总执行时间，单位：纳秒
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    /**
     * 重置操作执行成功的最大执行时间，单位：纳秒
     */
    public void resetMaxSuccessExecutionTime() {
        maxSuccessExecutionTime = 0;
    }

    /**
     * 重置每秒最大操作执行数
     */
    public void resetPeakTps() {
        peakTps = 0;
    }

    private void onExecuted(long estimatedTime) {
        MonitorUtil.safeAdd(totalCount, 1); //操作执行总次数 +1
        MonitorUtil.safeAdd(totalExecutionTime, estimatedTime); //操作总执行时间增加

        //计算每秒操作执行次数，非精确计算
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - currentTpsTimestamp <= 1000) {
            currentTps.incrementAndGet();
        } else {
            long currentTpsValue = currentTps.get();
            currentTps.set(0);
            currentTpsTimestamp = currentTimestamp;
            if (currentTpsValue > peakTps) {
                peakTps = currentTpsValue;
            }
        }
    }

    @Override
    public String toString() {
        return "ExecutionMonitor{" +
                "totalCount=" + totalCount +
                ", successCount=" + successCount +
                ", errorCountMap=" + errorCountMap +
                ", peakTps=" + peakTps +
                ", currentTps=" + currentTps +
                ", currentTpsTimestamp=" + currentTpsTimestamp +
                ", maxSuccessExecutionTime=" + maxSuccessExecutionTime +
                ", totalExecutionTime=" + totalExecutionTime +
                '}';
    }
}
