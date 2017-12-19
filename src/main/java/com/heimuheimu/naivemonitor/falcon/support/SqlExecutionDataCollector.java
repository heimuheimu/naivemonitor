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

package com.heimuheimu.naivemonitor.falcon.support;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.SqlExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.factory.NaiveSqlExecutionMonitorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL 语句执行信息采集器
 *
 * @author heimuheimu
 */
public class SqlExecutionDataCollector extends AbstractExecutionDataCollector {

    private static final Map<Integer, String> ERROR_METRIC_SUFFIX_MAP;

    static {
        ERROR_METRIC_SUFFIX_MAP = new HashMap<>();
        ERROR_METRIC_SUFFIX_MAP.put(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR, "_error");
        ERROR_METRIC_SUFFIX_MAP.put(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION, "_slow_execution");
    }

    /**
     * 当前监控数据所在的模块名称
     */
    private final String moduleName;

    /**
     * 当前采集器名称
     */
    private final String collectorName;


    /**
     * SQL 语句执行信息采集器
     */
    private final SqlExecutionMonitor sqlExecutionMonitor;

    private final List<ExecutionMonitor> executionMonitorList;

    /**
     * 构造一个 SQL 语句执行信息采集器
     *
     * @param moduleName 模块名称
     * @param dbName 需要采集信息的数据库名称
     */
    public SqlExecutionDataCollector(String moduleName, String dbName) {
        this.moduleName = moduleName;
        this.collectorName = dbName + "_sql";
        this.sqlExecutionMonitor = NaiveSqlExecutionMonitorFactory.get(dbName);
        executionMonitorList = new ArrayList<>();
        executionMonitorList.add(sqlExecutionMonitor.getExecutionMonitor());
    }


    @Override
    public int getPeriod() {
        return 30;
    }

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();
        falconDataList.addAll(super.getList());

        falconDataList.add(create("_max_result_size", sqlExecutionMonitor.getMaxResultSize()));
        sqlExecutionMonitor.resetMaxResultSize();

        falconDataList.add(create("_max_updated_rows", sqlExecutionMonitor.getMaxUpdatedRows()));
        sqlExecutionMonitor.resetMaxUpdatedRows();

        falconDataList.add(create("_max_deleted_rows", sqlExecutionMonitor.getMaxDeletedRows()));
        sqlExecutionMonitor.resetMaxDeletedRows();

        return falconDataList;
    }

    @Override
    protected List<ExecutionMonitor> getExecutionMonitorList() {
        return executionMonitorList;
    }

    @Override
    protected String getModuleName() {
        return moduleName;
    }

    @Override
    protected String getCollectorName() {
        return collectorName;
    }

    @Override
    protected Map<Integer, String> getErrorMetricSuffixMap() {
        return ERROR_METRIC_SUFFIX_MAP;
    }

}
