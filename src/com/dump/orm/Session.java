package com.dump.orm;

import com.dump.orm.util.JDBCUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Session {
    private static Session ourInstance = new Session();

    private Session() {
    }

    public static Session getSession() {
        return ourInstance;
    }

    private String[] getP(Object obj) {
        Class clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String pri = this.getPri(className);
        Field[] fields = clazz.getDeclaredFields();
        String[] p = new String[]{"", "", ""};

        for(int i = 0; i < fields.length; ++i) {
            Field f = fields[i];

            try {
                f.setAccessible(true);
                if(f.getName().equals(pri)) {
                    p[2] = f.get(obj).toString();
                }

                if(!f.getName().equals(pri) || !f.get(obj).equals("0")) {
                    p[0] = p[0] + f.getName() + ",";
                    p[1] = p[1] + "\'" + f.get(obj) + "\'" + ",";
                }
            } catch (IllegalAccessException var10) {
                var10.printStackTrace();
            }
        }

        p[0] = p[0].substring(0, p[0].length() - 1);
        p[1] = p[1].substring(0, p[1].length() - 1);
        return p;
    }

    public void save(Object obj) {
        Class clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = this.getP(obj);
        String sql = "insert into " + className + "(" + p[0] + ") values(" + p[1] + ");";
        JDBCUtil.executeUpdate(sql, new Object[0]);
    }

    public void delete(Object obj) {
        Class clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = this.getP(obj);
        String sql = "delete from " + className + " where " + this.getPri(className) + "=" + p[2];
        JDBCUtil.executeUpdate(sql, new Object[0]);
    }

    public void delete(Class<?> clazz, int id) {
        Object obj = null;

        try {
            obj = clazz.newInstance();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        String className = clazz.getSimpleName();
        this.getP(obj);
        String sql = "delete from " + className + " where " + this.getPri(className) + "=" + id;
        JDBCUtil.executeUpdate(sql, new Object[0]);
    }

    public void update(Object obj) {
        Class clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = this.getP(obj);
        String[] p1 = p[0].split(",");
        String[] p2 = p[1].split(",");
        String modify = "";

        for(int sql = 0; sql < p1.length; ++sql) {
            modify = modify + p1[sql] + "=" + p2[sql] + ",";
        }

        modify = modify.substring(0, modify.length() - 1);
        String var9 = "update " + className + " set " + modify + " where " + this.getPri(className) + "=" + p[2];
        JDBCUtil.executeUpdate(var9, new Object[0]);
    }

    public Object load(Class clazz, int id) {
        String className = clazz.getSimpleName();
        Object obj = null;

        try {
            obj = clazz.newInstance();
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        this.getP(obj);
        String sql = "select * from " + className + " where " + this.getPri(className) + "=" + id;
        ResultSet rs = JDBCUtil.executeQuery(sql, new Object[0]);
        Field[] fields = clazz.getDeclaredFields();

        try {
            if(rs.next()) {
                for(int e = 0; e < fields.length; ++e) {
                    fields[e].setAccessible(true);
                    fields[e].set(obj, rs.getObject(fields[e].getName()));
                }
            }

            JDBCUtil.freeResultSet(rs);
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return obj;
    }

    public int getCount(Class clazz) {
        String className = clazz.getSimpleName();
        String sql = "select count(*) from " + className;
        ResultSet rs = JDBCUtil.executeQuery(sql, new Object[0]);
        int count = 0;

        try {
            if(rs.next()) {
                count = rs.getInt(1);
            }

            JDBCUtil.freeResultSet(rs);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return count;
    }

    private List doQuery(ResultSet rs, Class clazz) {
        ArrayList list = new ArrayList();
        Field[] fields = clazz.getDeclaredFields();

        try {
            while(rs.next()) {
                Object e = clazz.newInstance();

                for(int i = 0; i < fields.length; ++i) {
                    fields[i].setAccessible(true);
                    fields[i].set(e, rs.getObject(fields[i].getName()));
                }

                list.add(e);
            }

            JDBCUtil.freeResultSet(rs);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return list;
    }

    public List getAll(Class clazz) {
        String sql = "select * from " + clazz.getSimpleName();
        ResultSet rs = JDBCUtil.executeQuery(sql, new Object[0]);
        List list = this.doQuery(rs, clazz);
        return list;
    }

    public List selectBysql(String sql, Class clazz, Object... objs) {
        ResultSet rs = JDBCUtil.executeQuery(sql, objs);
        List list = this.doQuery(rs, clazz);
        return list;
    }

    public List selectByPage(String sql, Class clazz, int pageNo, int pageSize, Object... objs) {
        sql = sql + " limit " + (pageNo - 1) * pageSize + "," + pageSize;
        ResultSet rs = JDBCUtil.executeQuery(sql, objs);
        List list = this.doQuery(rs, clazz);
        return list;
    }

    private String getPri(String tableName) {
        String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where table_name=\'" + tableName + "\' AND COLUMN_KEY=\'PRI\';";
        ResultSet rs = JDBCUtil.executeQuery(sql, new Object[0]);

        try {
            if(rs.next()) {
                return rs.getString(1);
            }

            JDBCUtil.freeResultSet(rs);
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

        return "";
    }
}
