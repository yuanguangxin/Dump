package group.dump.orm;

import group.dump.exception.DumpException;
import group.dump.orm.util.JdbcUtil;
import group.dump.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yuanguangxin
 */
public class Session {
    private static Session instance = new Session();

    private Session() {
    }

    static Session getSession() {
        return instance;
    }

    public String[] getFieldAndValue(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        String[] result = new String[]{"", ""};

        for (Field f : fields) {
            try {
                f.setAccessible(true);
                if (f.get(obj) != null) {
                    result[0] = result[0].concat(StringUtils.camelToUnderline(f.getName()) + ",");
                    result[1] = result[1].concat("'" + f.get(obj) + "'" + ",");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (!"".equals(result[0])) {
            result[0] = result[0].substring(0, result[0].length() - 1);
        }
        if (!"".equals(result[1])) {
            result[1] = result[1].substring(0, result[1].length() - 1);
        }
        return result;
    }

    public int save(Object obj) {
        Class<?> clazz = obj.getClass();
        String tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        String[] fieldValues = this.getFieldAndValue(obj);
        String sql = "insert into " + tableName + "(" + fieldValues[0] + ") values(" + fieldValues[1] + ");";
        return JdbcUtil.executeUpdate(sql);
    }

    public int delete(Object obj) {
        Class<?> clazz = obj.getClass();
        String tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        String priName = this.getPri(tableName);
        if (priName == null) {
            throw new DumpException("can not find primary key for table '" + tableName + "'");
        }
        String sql = "";
        try {

            Field priField = clazz.getDeclaredField(StringUtils.underlineToCamel(priName));
            priField.setAccessible(true);
            sql = "delete from " + tableName + " where " + priName + "=" + priField.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JdbcUtil.executeUpdate(sql);
    }

    public int delete(Class<?> clazz, int id) {
        String tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        String sql = "delete from " + tableName + " where " + getPri(tableName) + "=" + id;
        return JdbcUtil.executeUpdate(sql);
    }

    public int update(Object obj) {
        Class<?> clazz = obj.getClass();
        String tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        String[] fieldValues = this.getFieldAndValue(obj);
        String[] fields = fieldValues[0].split(",");
        String[] values = fieldValues[1].split(",");
        String modify = "";
        String priName = this.getPri(tableName);

        for (int sql = 0; sql < fields.length; ++sql) {
            modify = modify.concat(StringUtils.camelToUnderline(fields[sql]) + "=" + values[sql] + ",");
        }

        modify = modify.substring(0, modify.length() - 1);
        String str = null;
        try {
            Field priField = clazz.getDeclaredField(StringUtils.underlineToCamel(priName));
            priField.setAccessible(true);
            str = "update " + tableName + " set " + modify + " where " + priName + "=" + priField.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JdbcUtil.executeUpdate(str);
    }

    public <T> T load(Class<T> clazz, int id) {
        String tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        T obj = null;

        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = "select * from " + tableName + " where " + this.getPri(tableName) + "=" + id;
        ResultSet rs = JdbcUtil.executeQuery(sql);
        Field[] fields = clazz.getDeclaredFields();

        try {
            if (!rs.next()) {
                return null;
            }

            rs.beforeFirst();
            while (rs.next()) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    field.set(obj, rs.getObject(StringUtils.camelToUnderline(field.getName())));
                }
            }

            JdbcUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    public int getCount(Class<?> clazz) {
        String tableName = clazz.getSimpleName();
        String sql = "select count(1) from " + tableName;
        ResultSet rs = JdbcUtil.executeQuery(sql);
        int count = 0;

        try {
            if (rs.next()) {
                count = rs.getInt(1);
            }

            JdbcUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    private <T> List<T> doQuery(ResultSet rs, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        try {
            while (rs.next()) {
                T instance = clazz.newInstance();

                for (int i = 0; i < fields.length; ++i) {
                    fields[i].setAccessible(true);
                    fields[i].set(instance, rs.getObject(StringUtils.camelToUnderline(fields[i].getName())));
                }

                list.add(instance);
            }

            JdbcUtil.freeResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public <T> List<T> getAll(Class<T> clazz) {
        String sql = "select * from " + StringUtils.camelToUnderline(clazz.getSimpleName());
        ResultSet rs = JdbcUtil.executeQuery(sql);
        List<T> list = this.doQuery(rs, clazz);
        return list;
    }

    public <T> List<T> selectBySql(String sql, Class<T> clazz, Object... objs) {
        sql = "select * from " + StringUtils.camelToUnderline(clazz.getSimpleName()) + " where " + sql;
        ResultSet rs = JdbcUtil.executeQuery(sql, objs);
        List<T> list = this.doQuery(rs, clazz);
        return list;
    }

    public <T> List<T> selectByPage(String sql, Class<T> clazz, int pageNo, int pageSize, Object... objs) {
        sql = "select * from " + StringUtils.camelToUnderline(clazz.getSimpleName()) + " where " + sql + " limit " + (pageNo - 1) * pageSize + "," + pageSize;
        ResultSet rs = JdbcUtil.executeQuery(sql, objs);
        List<T> list = this.doQuery(rs, clazz);
        return list;
    }

    private String getPri(String tableName) {
        return JdbcUtil.getTablePri(tableName);
    }
}
