package com.dump.orm.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
/**
 * JDBCUtil  管理java连接数据库事务
 */

public class JDBCUtil {
    //初始化
    static{
        init();
        initPool();
    }
    private static ArrayList<Connection> pool;                           //自定义连接池
    private static Properties property;									 //配置文件
    private static String url;											 //url
    private static int min_size;										 //初始连接池大小
    private static int max_size;										 //最大连接池大小
    //初始化连接配置及自定义连接池
    private static void init() {
        pool = new ArrayList<>();
        property = new Properties();
        try {
            //读取配置文件
            InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
            property.load(is);
        } catch (FileNotFoundException e) {
            System.out.println("property file not found");
            return;
        } catch (IOException e) {
            System.out.println("unknown io error when loading property file");
            return;
        }
        //获取url
        url = property.getProperty("url");
        String driver = property.getProperty("driver");
        min_size = Integer.parseInt(property.getProperty("min_conn"));
        max_size = Integer.parseInt(property.getProperty("max_conn"));
        try {
            //注册驱动
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.out.println("driver not found");
            return;
        }
        initPool();
    }
    private static void initPool(){
        pool.clear();
        for(int i = 0; i < min_size ; i++){
            try {
                //向自定义池中添加连接
                pool.add(DriverManager.getConnection(url, property));
            } catch (SQLException e) {
                System.out.println("connection failed when building connections");
                return;
            }
        }
    }
    //获取连接
    private static Connection getConnection(){
        Connection conn = null;
        if(pool.size() <= 0){
            try {
                conn = DriverManager.getConnection(url, property);
            } catch (SQLException e) {
                System.out.println("connection failed when add connections");
                return null;
            }
        }else{
            conn = pool.remove(0);
            try {
                if(conn == null || conn.isClosed()){
                    initPool();
                    conn = pool.remove(0);
                }
            } catch (SQLException e) {
                System.out.println("close Connection failed");
            }
        }
        return conn;
    }
    //执行更新操作
    public static int executeUpdate(String sql , Object... o){
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            //注册事件
            assert conn != null;
            ps = conn.prepareStatement(sql);
        } catch (Exception e) {
            System.out.println("prepare statement failed");
            return freeConnection(conn);
        }
        try {
            ps.executeQuery("show tables");
        } catch (SQLException e1) {
            initPool();
            return executeUpdate(sql , o);
        }
        //设置参数
        if(o != null){
            for(int i = 0 ; i < o.length ; i ++ ){
                try {
                    ps.setObject(i + 1 , o[i]);
                } catch (SQLException e) {
                    System.out.println("set object failed");
                    return freeConnection(conn);
                }
            }
        }
        int i = -1;
        try {
            i = ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("update failed sql is "+sql);
            return freeConnection(conn);
        }
        freeConnection(conn);
        return i;
    }


    public static ResultSet executeQuery(String sql , Object... o){
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            //注册事件
            ps = conn.prepareStatement(sql);
        } catch (Exception e) {
            System.out.println("prepare statement failed");
            freeConnection(conn);
            return null;
        }
        try {
            ps.executeQuery("show tables");
        } catch (SQLException e1) {
            initPool();
            return executeQuery(sql , o);
        }
        //设置参数
        if(o!= null){
            for(int i = 0 ; i < o.length ; i ++ ){
                try {
                    ps.setObject(i + 1 , o[i]);
                } catch (SQLException e) {
                    System.out.println("set object failed");
                    freeConnection(conn);
                    return null;
                }
            }
        }
        ResultSet res = null;
        try {
            res = ps.executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("query failed sql is "+sql);
            return null;
        }
        freeConnection(conn);
        return res;
    }

    //释放连接
    private static int freeConnection(Connection conn){
        if( conn!= null){
            if(pool.size() < max_size){
                pool.add(conn);
            }else{
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("closing connection failed");
                }
            }
        }
        return -1;
    }
    //关闭结果集
    public static void freeResultSet(ResultSet res){
        if(res != null){
            try {
                res.close();
            } catch (SQLException e) {
                System.out.println("closing ResultSet failed");
                return;
            }
        }
    }
}
