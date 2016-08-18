package com.dump.proxy;

import java.lang.reflect.Method;

import com.dump.proxy.annotation.After;
import com.dump.proxy.annotation.Before;
import com.dump.proxy.annotation.Pointcut;
import net.sf.cglib.proxy.CallbackFilter;

/**
 * 动态代理方法过滤
 */
public class ProxyFilter implements CallbackFilter {
    @Override
    public int accept(Method method) {
        if(method.getDeclaredAnnotation(Pointcut.class)!=null){
            return 0;
        }else {
            return 1;
        }
    }
}
