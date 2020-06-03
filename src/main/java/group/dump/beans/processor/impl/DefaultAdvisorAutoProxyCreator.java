package group.dump.beans.processor.impl;

import group.dump.aop.proxy.ProxyInstance;
import group.dump.beans.processor.BeanPostProcessor;

/**
 * @author yuanguangxin
 */
public class DefaultAdvisorAutoProxyCreator implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object proxyBean = new ProxyInstance().getProxy(bean.getClass());
        return proxyBean == null ? bean : proxyBean;
    }
}
