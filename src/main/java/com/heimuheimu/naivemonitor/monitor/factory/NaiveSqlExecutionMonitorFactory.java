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

import com.heimuheimu.naivemonitor.monitor.SqlExecutionMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL 语句执行信息监控器工厂类
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class NaiveSqlExecutionMonitorFactory {

    private NaiveSqlExecutionMonitorFactory() {
        //prevent construct this class
    }

    public static final int ERROR_CODE_SQL_ERROR = -1;

    public static final int ERROR_CODE_SLOW_EXECUTION = -2;

    private static final ConcurrentHashMap<String, SqlExecutionMonitor> SQL_EXECUTION_MONITOR_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    /**
     * 根据数据库名称获得对应的 SQL 语句执行信息监控器，该方法不会返回 {@code null}
     *
     * @param dbName 数据库名称
     * @return 数据库名称获得对应的 SQL 语句执行信息监控器，该方法不会返回 {@code null}
     */
    public static SqlExecutionMonitor get(String dbName) {
        SqlExecutionMonitor monitor = SQL_EXECUTION_MONITOR_MAP.get(dbName);
        if (monitor == null) {
            synchronized (lock) {
                monitor = SQL_EXECUTION_MONITOR_MAP.get(dbName);
                if (monitor == null) {
                    monitor = new SqlExecutionMonitor();
                    SQL_EXECUTION_MONITOR_MAP.put(dbName, monitor);
                }
            }
        }
        return monitor;
    }

    /**
     * 获得当前 SQL 语句执行信息监控器工厂类所管理的所有 SQL 语句执行信息监控器列表
     *
     * @return 所有 SQL 语句执行信息监控器列表
     */
    public static List<SqlExecutionMonitor> getAll() {
        return new ArrayList<>(SQL_EXECUTION_MONITOR_MAP.values());
    }

}
