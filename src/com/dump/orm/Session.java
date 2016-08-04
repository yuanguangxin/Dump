package com.dump.orm;

import com.dump.orm.util.JDBCUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Session {
    private Session() {
    }

    private static Session ourInstance = new Session();

    public static Session getSession() {
        return ourInstance;
    }

    private String[] getP(Object obj) {
        Class<?> clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String pri = getPri(className);
        Field[] fields = clazz.getDeclaredFields();
        String[] p = new String[]{"", "", ""};
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            try {
                f.setAccessible(true);
                if(f.getName().equals(pri)) p[2] = f.get(obj).toString();
                if (f.getName().equals(pri) && f.get(obj).equals("0")) {
                    continue;
                }
                p[0] += f.getName() + ",";
                p[1] += "'" + f.get(obj) + "'" + ",";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        p[0] = p[0].substring(0, p[0].length() - 1);
        p[1] = p[1].substring(0, p[1].length() - 1);
        return p;
    }

    public void save(Object obj) {
        Class<?> clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = getP(obj);
        String sql = "insert into " + className + "(" + p[0] + ") values(" + p[1] + ");";
        JDBCUtil.executeUpdate(sql);
    }

    public void delete(Object obj){
        Class<?> clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = getP(obj);
        String sql = "delete from "+className+" where "+getPri(className)+"="+p[2];
        JDBCUtil.executeUpdate(sql);
    }

    public void delete(Class<?> clazz,int id){
        Object obj = null;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String className = clazz.getSimpleName();
        String[] p = getP(obj);
        String sql = "delete from "+className+" where "+getPri(className)+"="+id;
        JDBCUtil.executeUpdate(sql);
    }

    public void update(Object obj){
        Class<?> clazz = obj.getClass();
        String className = clazz.getSimpleName();
        String[] p = getP(obj);
        String[] p1 = p[0].split(",");
        String[] p2 = p[1].split(",");
        String modify = "";
        for (int i=0;i<p1.length;i++){
            modify += p1[i]+"="+p2[i]+",";
        }
        modify = modify.substring(0,modify.length()-1);
        String sql = "update "+className +" set "+modify+" where "+getPri(className)+"="+p[2];
        JDBCUtil.executeUpdate(sql);
    }

    public Object load(Class clazz,int id){
        String className = clazz.getSimpleName();
        Object obj = null;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] p = getP(obj);
        String sql = "select * from "+className +" where "+getPri(className)+"="+id;
        ResultSet rs = JDBCUtil.executeQuery(sql);
        Field[] fields = clazz.getDeclaredFields();
        try {
            if (rs.next()){
                for (int i=0;i<fields.length;i++){
                    fields[i].setAccessible(true);
                    fields[i].set(obj,rs.getObject(i+1));
                }
            }
            JDBCUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public int getCount(Class clazz){
        String className = clazz.getSimpleName();
        String sql = "select count(*) from "+className;
        ResultSet rs = JDBCUtil.executeQuery(sql);
        int count = 0;
        try {
            if (rs.next()){
                count = rs.getInt(1);
            }
            JDBCUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private List doQuery(ResultSet rs,Class clazz){
        List list = new ArrayList();
        Field[] fields = clazz.getDeclaredFields();
        try {
            while (rs.next()){
                Object obj = clazz.newInstance();
                for (int i=0;i<fields.length;i++){
                    fields[i].setAccessible(true);
                    fields[i].set(obj,rs.getObject(i+1));
                }
                list.add(obj);
            }
            JDBCUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List getAll(Class clazz){
        String sql = "select * from "+clazz.getSimpleName();
        ResultSet rs = JDBCUtil.executeQuery(sql);
        List list = doQuery(rs,clazz);
        return list;
    }

    public List selectBysql(String sql,Class clazz,Object... objs){
        ResultSet rs = JDBCUtil.executeQuery(sql,objs);
        List list = doQuery(rs,clazz);
        return list;
    }


    public List selectByPage(String sql, Class clazz, int pageNo, int pageSize, Object... objs){
        sql = sql+" limit "+ ((pageNo - 1) * pageSize)+","+pageSize;
        ResultSet rs = JDBCUtil.executeQuery(sql,objs);
        List list = doQuery(rs,clazz);
        return list;
    }

    private String getPri(String tableName) {
        String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where table_name='" + tableName + "' AND COLUMN_KEY='PRI';";
        ResultSet rs = JDBCUtil.executeQuery(sql);
        try {
            if (rs.next()) {
                return rs.getString(1);
            }
            JDBCUtil.freeResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

}
