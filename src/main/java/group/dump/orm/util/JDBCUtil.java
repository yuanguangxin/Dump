package group.dump.orm.util;

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
 * JDBCUtil  管理java连接数据库连接数
 */

public class JDBCUtil {
    //初始化
    static {
        init();
        initPool();
    }

    private static ArrayList<Connection> pool;
    private static Properties property;
    private static String url;
    private static int min_size;
    private static int max_size;

    private static void init() {
        pool = new ArrayList<>();
        property = new Properties();
        try {
            InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("dump.properties");
            property.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        url = property.getProperty("url");
        String driver = property.getProperty("driver");
        min_size = Integer.parseInt(property.getProperty("min_conn"));
        max_size = Integer.parseInt(property.getProperty("max_conn"));
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        initPool();
    }

    private static void initPool() {
        pool.clear();
        for (int i = 0; i < min_size; i++) {
            try {
                pool.add(DriverManager.getConnection(url, property));
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static Connection getConnection() {
        Connection conn = null;
        if (pool.size() <= 0) {
            try {
                conn = DriverManager.getConnection(url, property);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            conn = pool.remove(0);
            try {
                if (conn == null || conn.isClosed()) {
                    initPool();
                    conn = pool.remove(0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    public static int executeUpdate(String sql, Object... o) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            assert conn != null;
            ps = conn.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return freeConnection(conn);
        }
        try {
            ps.executeQuery("show tables");
        } catch (SQLException e1) {
            initPool();
            return executeUpdate(sql, o);
        }
        if (o != null) {
            for (int i = 0; i < o.length; i++) {
                try {
                    ps.setObject(i + 1, o[i]);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return freeConnection(conn);
                }
            }
        }
        int i = -1;
        try {
            i = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return freeConnection(conn);
        }
        freeConnection(conn);
        return i;
    }


    public static ResultSet executeQuery(String sql, Object... o) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
            freeConnection(conn);
            return null;
        }
        try {
            ps.executeQuery("show tables");
        } catch (SQLException e1) {
            initPool();
            return executeQuery(sql, o);
        }
        if (o != null) {
            for (int i = 0; i < o.length; i++) {
                try {
                    ps.setObject(i + 1, o[i]);
                } catch (SQLException e) {
                    e.printStackTrace();
                    freeConnection(conn);
                    return null;
                }
            }
        }
        ResultSet res = null;
        try {
            res = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        freeConnection(conn);
        return res;
    }

    private static int freeConnection(Connection conn) {
        if (conn != null) {
            if (pool.size() < max_size) {
                pool.add(conn);
            } else {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public static void freeResultSet(ResultSet res) {
        if (res != null) {
            try {
                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
