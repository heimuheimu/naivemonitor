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
import com.heimuheimu.naivemonitor.util.CollectionUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link SqlExecutionMonitor} 工厂类。
 *
 * <p><strong>说明：</strong>{@code NaiveSqlExecutionMonitorFactory} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class NaiveSqlExecutionMonitorFactory {

    private NaiveSqlExecutionMonitorFactory() {
        //prevent construct this class
    }

    /**
     * 错误码：SQL 语句执行错误
     */
    public static final int ERROR_CODE_SQL_ERROR = -1;

    /**
     * 错误码：SQL 慢查
     */
    public static final int ERROR_CODE_SLOW_EXECUTION = -2;

    private static final ConcurrentHashMap<String, SqlExecutionMonitor> SQL_EXECUTION_MONITOR_MAP = new ConcurrentHashMap<>();

    /**
     * 根据数据库名称获得对应的 {@code SqlExecutionMonitor} 实例，相同名称将返回同一个{@code SqlExecutionMonitor} 实例，
     * 该方法不会返回 {@code null}。
     *
     * @param dbName 数据库名称，不允许为 {@code null}
     * @return {@code SqlExecutionMonitor} 实例，不会返回 {@code null}
     */
    public static SqlExecutionMonitor get(String dbName) {
        return SQL_EXECUTION_MONITOR_MAP.computeIfAbsent(dbName, key -> new SqlExecutionMonitor());
    }

    /**
     * 获得当前 SqlExecutionMonitor 工厂管理的所有 SqlExecutionMonitor 实例列表，该方法不会返回 {@code null}。
     *
     * @return 当前 SqlExecutionMonitor 工厂管理的所有 SqlExecutionMonitor 实例列表
     * @since 1.1
     */
    public static List<SqlExecutionMonitor> getAll() {
        return CollectionUtil.getListByPrefix(SQL_EXECUTION_MONITOR_MAP, null);
    }

    /**
     * 根据名称前缀获得对应的 SqlExecutionMonitor 实例列表，该方法不会返回 {@code null}。
     *
     * @param prefix 名称前缀，如果为 {@code null} 或空，将会返回所有 SqlExecutionMonitor 实例列表
     * @return SqlExecutionMonitor 实例列表
     * @since 1.1
     */
    public static List<SqlExecutionMonitor> getListByPrefix(String prefix) {
        return CollectionUtil.getListByPrefix(SQL_EXECUTION_MONITOR_MAP, prefix);
    }
}
