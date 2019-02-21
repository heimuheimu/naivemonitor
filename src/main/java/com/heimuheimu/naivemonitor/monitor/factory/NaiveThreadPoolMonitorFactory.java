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

package com.heimuheimu.naivemonitor.monitor.factory;

import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ThreadPoolMonitor} 工厂类。
 *
 * <p><strong>说明：</strong>{@code NaiveThreadPoolMonitorFactory} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class NaiveThreadPoolMonitorFactory {

    private NaiveThreadPoolMonitorFactory() {
        //private constructor
    }

    private static final ConcurrentHashMap<String, ThreadPoolMonitor> THREAD_POOL_MONITOR_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    /**
     * 根据名称获得对应的 {@code ThreadPoolMonitor} 实例，相同名称将返回同一个{@code ThreadPoolMonitor} 实例，该方法不会返回 {@code null}。
     *
     * @param name 监控器名称，不允许为 {@code null}
     * @return {@code ThreadPoolMonitor} 实例，不会返回 {@code null}
     */
    public static ThreadPoolMonitor get(String name) {
        ThreadPoolMonitor monitor = THREAD_POOL_MONITOR_MAP.get(name);
        if (monitor == null) {
            synchronized (lock) {
                monitor = THREAD_POOL_MONITOR_MAP.get(name);
                if (monitor == null) {
                    monitor = new ThreadPoolMonitor();
                    THREAD_POOL_MONITOR_MAP.put(name, monitor);
                }
            }
        }
        return monitor;
    }

    /**
     * 获得当前 ThreadPoolMonitor 工厂管理的所有 ThreadPoolMonitor 实例列表，该方法不会返回 {@code null}。
     *
     * @return 当前 ThreadPoolMonitor 工厂管理的所有 ThreadPoolMonitor 实例列表
     */
    public static List<ThreadPoolMonitor> getAll() {
        return new ArrayList<>(THREAD_POOL_MONITOR_MAP.values());
    }
}
