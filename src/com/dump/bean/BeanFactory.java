package com.dump.bean;

import com.dump.proxy.DumpProxy;
import com.dump.proxy.ProxyInstance;
import com.dump.proxy.annotation.Aspect;
import com.dump.util.FindClassByAnnotationName;
import com.dump.util.PackagePath;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class BeanFactory {
    private static HashMap<String, HashMap<String, Object>> config = new HashMap();
    private static BeanFactory beanFactory = new BeanFactory();

    public static BeanFactory getBeanFactory() {
        Set cons = null;

        try {
            cons = FindClassByAnnotationName.getClass(PackagePath.getPath(), "com.dump.bean.annotation.Autowired");
            Iterator e = cons.iterator();

            while(e.hasNext()) {
                Class cl = (Class)e.next();
                HashMap beanConfig = new HashMap();
                beanConfig.put("class", cl.getName());
                config.put(cl.getSimpleName().substring(0, 1).toLowerCase() + cl.getSimpleName().substring(1), beanConfig);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return beanFactory;
    }

    private BeanFactory() {
    }

    public static Object getBean(String beanID) {
        HashMap beanConfig = (HashMap)config.get(beanID);
        String className = (String)beanConfig.get("class");
        Class bean = null;

        try {
            bean = Class.forName(className);
        } catch (ClassNotFoundException var15) {
            var15.printStackTrace();
        }

        Object instance = null;

        try {
            if(bean.getAnnotation(Aspect.class) == null) {
                instance = bean.newInstance();
            } else {
                instance = ProxyInstance.getAuthInstanceByFilter(bean, new DumpProxy());
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }

        Field[] e = bean.getDeclaredFields();
        int var6 = e.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Field field = e[var7];
            String fieldName = field.getName();
            Method method = null;

            try {
                method = bean.getDeclaredMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), new Class[]{field.getType()});
            } catch (Exception var13) {
                var13.printStackTrace();
            }

            try {
                method.invoke(instance, new Object[]{getBean(fieldName)});
            } catch (Exception var12) {
                var12.printStackTrace();
            }
        }

        return instance;
    }
}
