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

package com.heimuheimu.naivemonitor.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池使用情况信息统
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class ThreadPoolMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolMonitor.class);

    /**
     * 当前正在使用的线程池列表
     */
    private final CopyOnWriteArrayList<ThreadPoolExecutor> currentThreadPoolList = new CopyOnWriteArrayList<>();

    /**
     * 线程池拒绝执行的任务总数
     */
    private final AtomicLong rejectedCount = new AtomicLong();

    /**
     * 将该线程池加入到统计的线程池列表中，信息统计将包含该线程池的使用情况。当该线程池关闭后，会自动从统计的线程池列表中移除
     *
     * @param executor 需要进行统计的线程池
     */
    public void register(ThreadPoolExecutor executor) {
        if (executor != null) {
            currentThreadPoolList.add(executor);
        }
    }

    /**
     * 增加一次线程池拒绝执行的任务次数
     */
    public void addRejectedCount() {
        rejectedCount.incrementAndGet();
    }

    /**
     * 获得线程池拒绝执行的任务总数
     *
     * @return 线程池拒绝执行的任务总数
     */
    public long getRejectedCount() {
        return rejectedCount.get();
    }

    /**
     * 获得所有线程池中当前活跃线程数近似值总和
     *
     * @return 所有线程池中当前活跃线程数近似值总和
     */
    public int getActiveCount() {
        int activeCount = 0;
        try {
            for (ThreadPoolExecutor executor : currentThreadPoolList) {
                if (executor != null) {
                    if (!executor.isShutdown()) {
                        activeCount += executor.getActiveCount();
                    } else {
                        currentThreadPoolList.remove(executor);
                    }
                }
            }
            return activeCount;
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Current thread pool list: `" + currentThreadPoolList + "`.", e);
            return -1;
        }
    }

    /**
     * 获得所有线程池配置的核心线程数总和
     *
     * @return 所有线程池配置的核心线程数总和
     */
    public int getCorePoolSize() {
        int corePoolSize = 0;
        try {
            for (ThreadPoolExecutor executor : currentThreadPoolList) {
                if (executor != null) {
                    if (!executor.isShutdown()) {
                        corePoolSize += executor.getCorePoolSize();
                    } else {
                        currentThreadPoolList.remove(executor);
                    }
                }
            }
            return corePoolSize;
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Current thread pool list: `" + currentThreadPoolList + "`.", e);
            return -1;
        }
    }

    /**
     * 获得所有线程池配置的最大线程数总和
     *
     * @return 所有线程池配置的最大线程数总和
     */
    public int getMaximumPoolSize() {
        int maximumPoolSize = 0;
        try {
            for (ThreadPoolExecutor executor : currentThreadPoolList) {
                if (executor != null) {
                    if (!executor.isShutdown()) {
                        maximumPoolSize += executor.getMaximumPoolSize();
                    } else {
                        currentThreadPoolList.remove(executor);
                    }
                }
            }
            return maximumPoolSize;
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Current thread pool list: `" + currentThreadPoolList + "`.", e);
            return -1;
        }
    }

    /**
     * 获得所有线程池当前线程数总和
     *
     * @return 所有线程池当前线程数总和
     */
    public int getPoolSize() {
        int poolSize = 0;
        try {
            for (ThreadPoolExecutor executor : currentThreadPoolList) {
                if (executor != null) {
                    if (!executor.isShutdown()) {
                        poolSize += executor.getPoolSize();
                    } else {
                        currentThreadPoolList.remove(executor);
                    }
                }
            }
            return poolSize;
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Current thread pool list: `" + currentThreadPoolList + "`.", e);
            return -1;
        }
    }

    /**
     * 获得所有线程池出现过的最大线程数总和
     * <p>注意：不同线程池出现最大线程数时间可能不一致</p>
     *
     * @return 所有线程池出现过的最大线程数总和
     */
    public int getPeakPoolSize() {
        int peakPoolSize = 0;
        try {
            for (ThreadPoolExecutor executor : currentThreadPoolList) {
                if (executor != null) {
                    if (!executor.isShutdown()) {
                        peakPoolSize += executor.getLargestPoolSize();
                    } else {
                        currentThreadPoolList.remove(executor);
                    }
                }
            }
            return peakPoolSize;
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Current thread pool list: `" + currentThreadPoolList + "`.", e);
            return -1;
        }
    }

}
