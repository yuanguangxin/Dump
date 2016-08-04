package com.dump.util;

import java.util.*;

public class FindClassByAnnotationName {
    public static Set<Class<?>> getClass(String pack, String AnnotationName) throws Exception{
        Set<Class<?>> classes = FindAllClasses.getClasses(pack);
        Set<Class<?>> annoClasses = new LinkedHashSet<>();
        Iterator<Class<?>> it = classes.iterator();
        Class<?> cl = Class.forName(AnnotationName);
        while (it.hasNext()){
            Class c = it.next();
            if(c.getAnnotation(cl)!=null) {
                annoClasses.add(c);
            }
        }
        return annoClasses;
    }
    public static Set<Class<?>> getMethodClass(Set<Class<?>> c,String methodName) throws Exception{
        Set<Class<?>> classes = new LinkedHashSet<>();
        Iterator<Class<?>> it = c.iterator();
        Class cl = Class.forName(methodName);
        while (it.hasNext()){
            Class cc = it.next();
            if(cc.getAnnotation(cl)!=null) {
                classes.add(cc);
            }
        }
        return classes;
    }
}
