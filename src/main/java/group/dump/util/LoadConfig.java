package group.dump.util;

import group.dump.orm.util.JDBCUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadConfig {
    public static String getPath(){
        Properties pps = new Properties();
        String key = "packagePath";
        try {
            InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("dump.properties");
            pps.load(is);
            String value = pps.getProperty(key);
            return value;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
