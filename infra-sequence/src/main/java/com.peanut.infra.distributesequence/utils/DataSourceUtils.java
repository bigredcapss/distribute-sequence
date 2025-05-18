package com.peanut.infra.distributesequence.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.peanut.infra.distributesequence.config.SegmentProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author peanut
 * @description 数据源工具类
 */
public final class DataSourceUtils {

    private static Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

    public static final String DS_DRUID_CLASS = "com.alibaba.druid.pool.DruidDataSource";

    public static final String DS_HIKARI_CLASS = "com.zaxxer.hikari.HikariDataSource";

    public static boolean isDruidDataSource() {
        try {
            Class.forName(DS_DRUID_CLASS, true, DataSourceUtils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isHikariDataSource() {
        try {
            Class.forName(DS_HIKARI_CLASS, true, DataSourceUtils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static DataSource createDruidDatasource(SegmentProperties properties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getSequenceJdbcUrl());
        dataSource.setUsername(properties.getSequenceJdbcUserName());
        dataSource.setPassword(properties.getSequenceJdbcPassWord());
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        try {
            dataSource.init();
        } catch (SQLException ex) {
            logger.error("init DruidDataSource error", ex);
        }
        return dataSource;
    }

    public static DataSource createHikariDatasource(SegmentProperties properties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(properties.getSequenceJdbcUrl());
        hikariConfig.setUsername(properties.getSequenceJdbcUserName());
        hikariConfig.setPassword(properties.getSequenceJdbcPassWord());
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }
}