package com.heimuheimu.naivemonitor.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import javax.sql.DataSource;

/**
 *
 */
public class SmartSqlMapClientTemplate extends SqlMapClientTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartSqlMapClientTemplate.class);

    private final String dbName;

    public SmartSqlMapClientTemplate(String dbName, SqlMapClient sqlMapClient) {
        super(sqlMapClient);
        this.dbName = dbName;
    }

    public SmartSqlMapClientTemplate(String dbName, DataSource dataSource, SqlMapClient sqlMapClient) {
        super(dataSource, sqlMapClient);
        this.dbName = dbName;
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
            LOGGER.error("Query for object failed: `" + e.getMessage() + "`. Statement name: `" + statementName
                    + "`. Parameter: `" + parameterObject + "`.", e);
            throw e;
        } finally {

        }
    }

    @Override
    public Object queryForObject(String statementName, Object parameterObject, Object resultObject) throws DataAccessException {
        return super.queryForObject(statementName, parameterObject, resultObject);
    }
}
