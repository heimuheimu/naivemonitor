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

package com.heimuheimu.naivemonitor.ibatis;

import com.heimuheimu.naivemonitor.monitor.SqlExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.factory.NaiveSqlExecutionMonitorFactory;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Spring 提供的 iBatis 帮助类实现的 SQL 执行器。通过封装 {@link SqlMapClientTemplate} ，来实现监控 SQL 语句执行信息。
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SmartSqlMapClientTemplate extends SqlMapClientTemplate {

    private static final Logger SQL_ERROR_EXECUTION_LOGGER = LoggerFactory.getLogger("NAIVESQL_ERROR_EXECUTION_LOGGER");

    private static final Logger SQL_SLOW_EXECUTION_LOGGER = LoggerFactory.getLogger("NAIVESQL_SLOW_EXECUTION_LOGGER");

    /**
     * 数据库名称
     */
    private final String dbName;

    /**
     * 大于该执行时间的 SQL 语句执行将会被定义为慢查，单位：纳秒
     */
    private final long slowExecutionThreshold;

    /**
     * SQL 语句执行信息监控器
     */
    private final SqlExecutionMonitor sqlExecutionMonitor;

    /**
     * 构造一个基于 Spring 提供的 iBatis 帮助类实现的 SQL 执行器
     *
     * @param dbName 数据库名称
     * @param sqlMapClient SQL 执行器使用的 sqlMapClient
     * @param slowExecutionThreshold 大于该执行时间的 SQL 语句执行将会被定义为慢查，单位：毫秒
     */
    public SmartSqlMapClientTemplate(String dbName, SqlMapClient sqlMapClient, long slowExecutionThreshold) {
        super(sqlMapClient);
        this.dbName = dbName;
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS);
        this.sqlExecutionMonitor = NaiveSqlExecutionMonitorFactory.get(dbName);
    }

    /**
     * 构造一个基于 Spring 提供的 iBatis 帮助类实现的 SQL 执行器
     *
     * @param dbName 数据库名称
     * @param dataSource SQL 执行器使用的数据库连接池
     * @param sqlMapClient SQL 执行器使用的 sqlMapClient
     * @param slowExecutionThreshold 大于该执行时间的 SQL 语句执行将会被定义为慢查，单位：毫秒
     */
    public SmartSqlMapClientTemplate(String dbName, DataSource dataSource, SqlMapClient sqlMapClient, long slowExecutionThreshold) {
        super(dataSource, sqlMapClient);
        this.dbName = dbName;
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS);
        this.sqlExecutionMonitor = NaiveSqlExecutionMonitorFactory.get(dbName);
    }

    @Override
    public Object queryForObject(String statementName) throws DataAccessException {
        return queryForObject(statementName, null);
    }

    @Override
    public Object queryForObject(String statementName, Object parameterObject) throws DataAccessException {
        long startTime = System.nanoTime();
        try {
            return super.queryForObject(statementName, parameterObject);
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for object failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForObject] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject);
            }
        }
    }

    @Override
    public Object queryForObject(String statementName, Object parameterObject, Object resultObject) throws DataAccessException {
        long startTime = System.nanoTime();
        try {
            return super.queryForObject(statementName, parameterObject, resultObject);
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for object failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Result object: `" + resultObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForObject] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Result object: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, resultObject);
            }
        }
    }

    @Override
    public List queryForList(String statementName) throws DataAccessException {
        return queryForList(statementName, null);
    }

    @Override
    public List queryForList(String statementName, Object parameterObject) throws DataAccessException {
        long startTime = System.nanoTime();
        int resultSize = 0;
        try {
            List result = super.queryForList(statementName, parameterObject);
            resultSize = result.size();
            sqlExecutionMonitor.onQueryList(resultSize);
            return result;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for list failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForList] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Result size: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, resultSize);
            }
        }

    }

    @Override
    public List queryForList(String statementName, int skipResults, int maxResults) throws DataAccessException {
        return queryForList(statementName, null, skipResults, maxResults);
    }

    @Override
    public List queryForList(String statementName, Object parameterObject, int skipResults, int maxResults) throws DataAccessException {
        long startTime = System.nanoTime();
        int resultSize = 0;
        try {
            List result = super.queryForList(statementName, parameterObject, skipResults, maxResults);
            resultSize = result.size();
            sqlExecutionMonitor.onQueryList(resultSize);
            return result;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for list failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Skip results: `" + skipResults + "`. Max results: `"
                    + maxResults + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForList] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Result size: `{}`. Skip results: `{}`. Max results: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, resultSize, skipResults, maxResults);
            }
        }
    }

    @Override
    public Map queryForMap(String statementName, Object parameterObject, String keyProperty) throws DataAccessException {
        long startTime = System.nanoTime();
        int resultSize = 0;
        try {
            Map result = super.queryForMap(statementName, parameterObject, keyProperty);
            resultSize = result.size();
            sqlExecutionMonitor.onQueryList(resultSize);
            return result;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for map failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Key property: `" + keyProperty + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForMap] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Key property: `{}`. Result size: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, keyProperty, resultSize);
            }
        }
    }

    @Override
    public Map queryForMap(String statementName, Object parameterObject, String keyProperty, String valueProperty) throws DataAccessException {
        long startTime = System.nanoTime();
        int resultSize = 0;
        try {
            Map result = super.queryForMap(statementName, parameterObject, keyProperty, valueProperty);
            resultSize = result.size();
            sqlExecutionMonitor.onQueryList(resultSize);
            return result;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Query for map failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Key property: `" + keyProperty + "`. Value property: `"
                    + valueProperty + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[queryForMap] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Key property: `{}`. Value property: `{}`. Result size: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, keyProperty, valueProperty, resultSize);
            }
        }
    }

    @Override
    public Object insert(String statementName) throws DataAccessException {
        return insert(statementName, null);
    }

    @Override
    public Object insert(String statementName, Object parameterObject) throws DataAccessException {
        long startTime = System.nanoTime();
        try {
            return super.insert(statementName, parameterObject);
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Insert failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[insert] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject);
            }
        }
    }

    @Override
    public int update(String statementName) throws DataAccessException {
        return update(statementName, null);
    }

    @Override
    public int update(String statementName, Object parameterObject) throws DataAccessException {
        long startTime = System.nanoTime();
        int updatedRows = 0;
        try {
            updatedRows = super.update(statementName, parameterObject);
            sqlExecutionMonitor.onUpdated(updatedRows);
            return updatedRows;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Update failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[update] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Updated rows: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, updatedRows);
            }
        }
    }

    @Override
    public void update(String statementName, Object parameterObject, int requiredRowsAffected) throws DataAccessException {
        long startTime = System.nanoTime();
        try {
            super.update(statementName, parameterObject, requiredRowsAffected);
            sqlExecutionMonitor.onUpdated(requiredRowsAffected);
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Update failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Required rows affected: `" + requiredRowsAffected
                    + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[update] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Required rows affected: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, requiredRowsAffected);
            }
        }
    }

    @Override
    public int delete(String statementName) throws DataAccessException {
        return delete(statementName, null);
    }

    @Override
    public int delete(String statementName, Object parameterObject) throws DataAccessException {
        long startTime = System.nanoTime();
        int deletedRows = 0;
        try {
            deletedRows = super.delete(statementName, parameterObject);
            sqlExecutionMonitor.onDeleted(deletedRows);
            return deletedRows;
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Delete failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[delete] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Deleted rows: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, deletedRows);
            }
        }
    }

    @Override
    public void delete(String statementName, Object parameterObject, int requiredRowsAffected) throws DataAccessException {
        long startTime = System.nanoTime();
        try {
            super.delete(statementName, parameterObject, requiredRowsAffected);
            sqlExecutionMonitor.onDeleted(requiredRowsAffected);
        } catch (Exception e) {
            sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SQL_ERROR);
            SQL_ERROR_EXECUTION_LOGGER.error("Delete failed: `" + e.getMessage() + "`. Db name: `" + dbName + "`. Statement name: `"
                    + statementName + "`. Parameter: `" + parameterObject + "`. Required rows affected: `" + requiredRowsAffected
                    + "`.", e);
            throw e;
        } finally {
            sqlExecutionMonitor.getExecutionMonitor().onExecuted(startTime);
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                sqlExecutionMonitor.getExecutionMonitor().onError(NaiveSqlExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION);
                SQL_SLOW_EXECUTION_LOGGER.error("[delete] Cost: `{}ns ({})ms`. Db name: `{}`. Statement name: `{}`. Parameter: `{}`. Required rows affected: `{}`.",
                        executedNanoTime, TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), dbName,
                        statementName, parameterObject, requiredRowsAffected);
            }
        }
    }
}
