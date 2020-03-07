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

package com.heimuheimu.naivemonitor.monitor.hotspot.classloading;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * JVM 类加载信息监控器。
 *
 * <p><strong>说明：</strong>ClassLoadingMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class ClassLoadingMonitor {

    /**
     * {@link #getInstance()} 方法返回的 ClassLoadingMonitor 实例，访问此变量需获得锁 "ClassLoadingMonitor.class"
     */
    private static ClassLoadingMonitor CLASS_LOADING_MONITOR = null;

    /**
     * 类加载管理器
     */
    private final ClassLoadingMXBean classLoadingMXBean;

    /**
     * 构造一个 ClassLoadingMonitor 实例。
     */
    private ClassLoadingMonitor() {
        this.classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    }

    /**
     * 获得 JVM 中当前已加载的类数量。
     *
     * @return 当前已加载的类数量
     */
    public int getLoadedClassCount() {
        return classLoadingMXBean.getLoadedClassCount();
    }

    /**
     * 获得 JVM 中累计加载过的类数量。
     *
     * @return 加载过的类数量
     */
    public long getTotalLoadedClassCount() {
        return classLoadingMXBean.getTotalLoadedClassCount();
    }

    /**
     * 获得 JVM 中累计卸载过的类数量。
     *
     * @return 卸载过的类数量
     */
    public long getTotalUnloadedClassCount() {
        return classLoadingMXBean.getUnloadedClassCount();
    }

    /**
     * 获得 ClassLoadingMonitor 实例，用于监控 JVM 类加载信息。
     *
     * @return ClassLoadingMonitor 实例
     */
    public static synchronized ClassLoadingMonitor getInstance() {
        if (CLASS_LOADING_MONITOR == null) {
            CLASS_LOADING_MONITOR = new ClassLoadingMonitor();
        }
        return CLASS_LOADING_MONITOR;
    }
}
