package group.dump.proxy;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import group.dump.proxy.annotation.After;
import group.dump.proxy.annotation.Before;
import group.dump.util.AnnotationUtil;
import group.dump.util.LoadConfig;
import net.sf.cglib.proxy.*;

public class DumpProxy implements MethodInterceptor{
    public Enhancer enhancer = new Enhancer();

    @Override
    public Object intercept(Object object, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {

        Set<Class<?>> cons = AnnotationUtil.getClass(LoadConfig.getPath(), "group.dump.proxy.annotation.Aspect");
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
