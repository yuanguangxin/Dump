package com.dump.proxy;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import com.dump.proxy.annotation.After;
import com.dump.proxy.annotation.Before;
import com.dump.util.FindClassByAnnotationName;
import net.sf.cglib.proxy.*;
import test.dao.StudentDao;
import test.model.Student;

public class DumpProxy implements MethodInterceptor{
    public Enhancer enhancer = new Enhancer();

    @Override
    public Object intercept(Object object, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {

        Set<Class<?>> cons = FindClassByAnnotationName.getClass("test", "com.dump.proxy.annotation.Aspect");
        Iterator<Class<?>> it = cons.iterator();
        while (it.hasNext()){
            Class<?> cl = it.next();
            if(cl.isInstance(object)){
                Method[] methods = cl.getDeclaredMethods();
                for(Method m:methods){
                    Before before = m.getAnnotation(Before.class);
                    if (before!=null){
                        Object obj = cl.newInstance();
                        m.invoke(obj,args);
                        break;
                    }
                }
            }
        }
        Object result = methodProxy.invokeSuper(object, args);
        it = cons.iterator();
        while (it.hasNext()){
            Class<?> cl = it.next();
            if(cl.isInstance(object)){
                Method[] methods = cl.getDeclaredMethods();
                for(Method m:methods){
                    After after = m.getAnnotation(After.class);
                    if (after!=null){
                        Object obj = cl.newInstance();
                        m.invoke(obj,args);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
