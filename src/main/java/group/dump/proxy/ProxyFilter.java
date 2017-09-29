package group.dump.proxy;

import java.lang.reflect.Method;

import group.dump.proxy.annotation.Pointcut;
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
