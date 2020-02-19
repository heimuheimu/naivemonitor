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

package com.heimuheimu.naivemonitor.monitor.hotspot.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * JVM 线程信息监控器。
 *
 * <p><strong>说明：</strong>ThreadMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ThreadMonitor {

    /**
     * {@link #getInstance()} 方法返回的 ThreadMonitor 实例，访问此变量需获得锁 "ThreadMonitor.class"
     */
    private static ThreadMonitor THREAD_MONITOR = null;

    /**
     * 线程管理器
     */
    private final ThreadMXBean threadMXBean;

    /**
     * 构造一个 ThreadMonitor 实例。
     */
    private ThreadMonitor() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }

    /**
     * 获得 JVM 中当前存活线程总数。
     *
     * @return 前存活线程总数
     */
    public long getThreadCount() {
        return threadMXBean.getThreadCount();
    }

    /**
     * 获得 JVM 中峰值存活线程总数。
     *
     * <p><strong>注意：</strong>本方法执行后会重置 JVM 中峰值存活线程总数。</p>
     *
     * @return 峰值存活线程总数
     */
    public long getPeakThreadCount() {
        long peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount();
        return peakThreadCount;
    }

    /**
     * 获得 JVM 中累计启动过的线程总数。
     *
     * @return 启动过的线程总数
     */
    public long getTotalStartedThreadCount() {
        return threadMXBean.getTotalStartedThreadCount();
    }

    /**
     * 获得 JVM 中当前存活的 Daemon 线程总数。
     *
     * @return 当前存活的 Daemon 线程总数
     */
    public long getDaemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }

    /**
     * 获得 ThreadMonitor 实例，用于监控 JVM 线程信息。
     *
     * @return ThreadMonitor 实例
     */
    public static synchronized ThreadMonitor getInstance() {
        if (THREAD_MONITOR == null) {
            THREAD_MONITOR = new ThreadMonitor();
        }
        return THREAD_MONITOR;
    }
}
