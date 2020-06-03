package group.dump.aop.proxy;

import group.dump.aop.util.AspectUtils;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author yuanguangxin
 */
public class DefaultBeanProxy implements MethodInterceptor {

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Method beforeMethod = AspectUtils.getBeforeAdvisorMethod(method.getName());
        if (beforeMethod != null) {
            beforeMethod.invoke(AspectUtils.getAdvisorInstance(beforeMethod.getDeclaringClass()), args);
        }

        Object result = methodProxy.invokeSuper(object, args);

        Method afterMethod = AspectUtils.getAfterAdvisorMethod(method.getName());
        if (afterMethod != null) {
            afterMethod.invoke(AspectUtils.getAdvisorInstance(afterMethod.getDeclaringClass()), args);
        }

        return result;
    }
}
