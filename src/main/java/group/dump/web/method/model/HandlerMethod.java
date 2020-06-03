package group.dump.web.method.model;

import java.lang.reflect.Method;

/**
 * @author yuanguangxin
 */
public class HandlerMethod {

    private Object bean;

    private Method method;

    public HandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
