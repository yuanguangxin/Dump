package group.dump.beans.util;

import group.dump.beans.ApplicationContext;

/**
 * @author yuanguangxin
 */
public class ApplicationContextUtils {

    private static ApplicationContext applicationContext;

    public static void refresh() {
        applicationContext = new ApplicationContext();
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }
}
