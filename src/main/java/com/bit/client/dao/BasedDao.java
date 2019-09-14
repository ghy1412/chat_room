package com.bit.client.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.bit.utils.CommUtil;


import java.sql.*;
import java.util.Properties;

/*
* dao层的顶层父类
*
* 封装数据源, 获取连接, 关闭资源等公有操作
* */
public class BasedDao {
    private static DruidDataSource DRUID_DATA_SOURCE;

    //加载数据源
    static {
        Properties properties =
                CommUtil.loadProperties("db.properties");
        try {
            DRUID_DATA_SOURCE = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            System.err.println("加载数据源失败");
            e.printStackTrace();
        }
    }

    //获取连接  因为是子类获取 所以子类权限
    protected Connection getConnection(){

        try {
            return  DRUID_DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            System.err.println("获取连接失败");
            e.printStackTrace();
        }
        return null;
    }

    //4.关闭资源

    protected void closeResources(Connection connection){
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //用重载的方法
    protected void closeResources(Connection connection, Statement statement){

        if (connection != null){
            closeResources(connection);
        }
        if (statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //用重载的方法
    protected void closeResources(Connection connection,
                                  PreparedStatement statement, ResultSet resultSet){

        if (connection != null && statement!= null ){
            closeResources(connection,statement);
        }

        if (resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
