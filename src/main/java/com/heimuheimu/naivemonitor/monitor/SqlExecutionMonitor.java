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

/**
 * SQL 语句执行信息监控器
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SqlExecutionMonitor {

    /**
     * SQL 语句执行监控器
     */
    private final ExecutionMonitor executionMonitor = new ExecutionMonitor();

    /**
     * 数据库单条 Select 语句返回的最大记录行数
     */
    private volatile long maxResultSize = 0;

    /**
     * 数据库单条 Update 语句更新的最大行数
     */
    private volatile long maxUpdatedRows = 0;

    /**
     * 数据库单条 Delete 语句删除的最大行数
     */
    private volatile long maxDeletedRows = 0;

    /**
     * 对数据库 Select 查询多条数据进行监控
     *
     * @param resultSize Select 语句查询的记录条数
     */
    public void onQueryList(int resultSize) {
        if (resultSize > maxResultSize) {
            maxResultSize = resultSize;
        }
    }

    /**
     * 对数据库 Update 操作进行监控
     *
     * @param updatedRows 实际更新的行数
     */
    public void onUpdated(int updatedRows) {
        if (updatedRows > maxUpdatedRows) {
            maxUpdatedRows = updatedRows;
        }
    }

    /**
     * 对数据库 Delete 操作进行监控
     *
     * @param deletedRows 实际删除的行数
     */
    public void onDeleted(int deletedRows) {
        if (deletedRows > maxDeletedRows) {
            maxDeletedRows = deletedRows;
        }
    }

    /**
     * 获得 SQL 语句执行监控器
     *
     * @return SQL 语句执行监控器
     */
    public ExecutionMonitor getExecutionMonitor() {
        return executionMonitor;
    }

    /**
     * 获得数据库单条 Select 语句返回的最大记录行数
     *
     * @return 数据库单条 Select 语句返回的最大记录行数
     */
    public long getMaxResultSize() {
        return maxResultSize;
    }

    /**
     * 获得数据库单条 Update 语句更新的最大行数
     *
     * @return 数据库单条 Update 语句更新的最大行数
     */
    public long getMaxUpdatedRows() {
        return maxUpdatedRows;
    }

    /**
     * 获得数据库单条 Delete 语句删除的最大行数
     *
     * @return 数据库单条 Delete 语句删除的最大行数
     */
    public long getMaxDeletedRows() {
        return maxDeletedRows;
    }

    /**
     * 重置数据库单条 Select 语句返回的最大记录行数
     */
    public void resetMaxResultSize() {
        maxResultSize = 0;
    }

    /**
     * 重置数据库单条 Update 语句更新的最大行数
     */
    public void resetMaxUpdatedRows() {
        maxUpdatedRows = 0;
    }

    /**
     * 重置数据库单条 Delete 语句删除的最大行数
     */
    public void resetMaxDeletedRows() {
        maxDeletedRows = 0;
    }

    @Override
    public String toString() {
        return "SqlExecutionMonitor{" +
                "executionMonitor=" + executionMonitor +
                ", maxResultSize=" + maxResultSize +
                ", maxUpdatedRows=" + maxUpdatedRows +
                ", maxDeletedRows=" + maxDeletedRows +
                '}';
    }
}
