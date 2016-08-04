package com.dump.bean;

import com.dump.proxy.DumpProxy;
import com.dump.proxy.ProxyInstance;
import com.dump.proxy.annotation.Aspect;
import com.dump.util.FindClassByAnnotationName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class BeanFactory {
    private HashMap<String, HashMap<String, Object>> config = new HashMap<>();

    public static BeanFactory getBeanFactory(){
        return new BeanFactory();
    }

    private BeanFactory() {
        Set<Class<?>> cons = null;
        try {
            cons = FindClassByAnnotationName.getClass("test", "com.dump.bean.annotation.Autowired");
            Iterator<Class<?>> it = cons.iterator();
            while (it.hasNext()) {
                Class<?> cl = it.next();
                HashMap<String, Object> beanConfig = new HashMap<>();
                beanConfig.put("class", cl.getName());
                config.put(cl.getSimpleName().substring(0, 1).toLowerCase() + cl.getSimpleName().substring(1), beanConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getBean(String beanID) {
        // 获取hashMap配置内容
        HashMap<String, Object> beanConfig = this.config.get(beanID);
        // 获取完整类名
        String className = (String) beanConfig.get("class");
        Class<?> bean = null;
        try {
            // 获取类类型实例
            bean = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        Object instance = null;
        try {
            // 获取这个类的一个实例
            if(bean.getAnnotation(Aspect.class)==null){
                instance = bean.newInstance();
            }else {
                instance = ProxyInstance.getAuthInstanceByFilter(bean,new DumpProxy());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Field field : bean.getDeclaredFields()) {
            // 获取属性名
            String fieldName = field.getName();
            Method method = null;
            try {
                method = bean.getDeclaredMethod(
                        "set" + fieldName.substring(0, 1).toUpperCase()
                                + fieldName.substring(1),
                        new Class[] { field.getType() });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                method.invoke(instance, new Object[] { getBean(fieldName) });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return instance;
    }
}
