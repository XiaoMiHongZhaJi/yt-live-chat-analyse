package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

public class JDBCUtil {

    private static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);

    /**
     * 获取数据库连接
     * @return
     */
    public static Connection getConnection(){
        Properties properties = new Properties();
        Connection connection = null;
        try {
            properties.load(new BufferedInputStream(new FileInputStream("src/main/resources/application.properties")));
            String url = properties.getProperty("spring.datasource.url");
            String username = properties.getProperty("spring.datasource.username");
            String password = properties.getProperty("spring.datasource.password");
            Class.forName(properties.getProperty("spring.datasource.driver-class-name"));
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            logger.error("获取数据库连接失败", e);
        }
        return connection;
    }

    /**
     * 查询多个字段
     * @param sql
     * @param params
     * @return
     */
    public static List<Map<String, String>> queryMapList(String sql, List<String> params){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(!CollectionUtils.isEmpty(params)){
                for (int i = 0; i < params.size(); i ++) {
                    String param = params.get(i);
                    ps.setString(i + 1, param);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()){
                Map<String, String> map = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    map.put(columnLabel, rs.getString(i));
                }
                list.add(map);
            }
        } catch (Exception e) {
            logger.error("查询失败", e);
        } finally {
            closeConnection(conn, ps, rs);
        }
        return list;
    }

    /**
     * 查询一个字段
     * @param sql
     * @param params
     * @return
     */
    public static List<String> queryStringList(String sql, List<String> params){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(!CollectionUtils.isEmpty(params)){
                for (int i = 0; i < params.size(); i ++) {
                    String param = params.get(i);
                    ps.setString(i + 1, param);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()){
                list.add(rs.getString(1));
            }
        } catch (Exception e) {
            logger.error("查询失败", e);
        } finally {
            closeConnection(conn, ps, rs);
        }
        return list;
    }

    /**
     * 更新
     * @param sql
     * @param params
     */
    public static int executeUpdate(String sql, List<String> params){
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(!CollectionUtils.isEmpty(params)){
                for (int i = 0; i < params.size(); i ++) {
                    String param = params.get(i);
                    if(StringUtils.isNumeric(param) && param.length() <= 10){
                        ps.setInt(i + 1, Integer.parseInt(param));
                    }else{
                        ps.setString(i + 1, param);
                    }
                }
            }
            return ps.executeUpdate();
        } catch (Exception e) {
            logger.error("更新失败", e);
        } finally {
            closeConnection(conn, ps);
        }
        return 0;
    }

    public static void closeStatement(PreparedStatement ps, ResultSet rs){
        try {
            if(rs != null){
                rs.close();
            }
            if(ps != null){
                ps.close();
            }
        } catch (Exception e) {
            logger.error("关闭连接失败", e);
        }
    }

    public static void closeConnection(Connection conn, PreparedStatement ps){
        closeConnection(conn, ps, null);
    }

    public static void closeConnection(Connection conn, PreparedStatement ps, ResultSet rs){
        try {
            if(rs != null){
                rs.close();
            }
            if(ps != null){
                ps.close();
            }
            if(conn != null){
                conn.close();
            }
        } catch (Exception e) {
            logger.error("关闭连接失败", e);
        }
    }
}
