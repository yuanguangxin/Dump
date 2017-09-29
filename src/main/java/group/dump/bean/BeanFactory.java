package group.dump.bean;

import group.dump.proxy.DumpProxy;
import group.dump.proxy.ProxyInstance;
import group.dump.proxy.annotation.Aspect;
import group.dump.util.AnnotationUtil;
import group.dump.util.LoadConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Dump的Bean容器,采用HashMap实现
 */
public class BeanFactory {
    private static Map<String, Map<String, Object>> config = new HashMap();
    private static BeanFactory beanFactory = new BeanFactory();

    public static BeanFactory getBeanFactory() {
        Set cons = null;

        try {
            cons = AnnotationUtil.getClass(LoadConfig.getPath(), "group.dump.bean.annotation.Autowired");
            Iterator e = cons.iterator();

            while(e.hasNext()) {
                Class cl = (Class)e.next();
                HashMap beanConfig = new HashMap();
                beanConfig.put("class", cl.getName());
                config.put(cl.getSimpleName().substring(0, 1).toLowerCase() + cl.getSimpleName().substring(1), beanConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return beanFactory;
    }

    private BeanFactory() {
    }

    public static Object getBean(String beanID) {
        HashMap beanConfig = (HashMap) config.get(beanID);
        String className = (String)beanConfig.get("class");
        Class bean = null;

        try {
            bean = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Object instance = null;

        try {
            if(bean.getAnnotation(Aspect.class) == null) {
                instance = bean.newInstance();
            } else {
                instance = ProxyInstance.getAuthInstanceByFilter(bean, new DumpProxy());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Field[] e = bean.getDeclaredFields();
        int n = e.length;

        for(int i = 0; i < n; ++i) {
            Field field = e[i];
            String fieldName = field.getName();
            Method method = null;
            try {
                method = bean.getDeclaredMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), new Class[]{field.getType()});
                method.invoke(instance, new Object[]{getBean(fieldName)});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return instance;
    }
}
