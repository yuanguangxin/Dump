package group.dump.beans.processor;

/**
 * @author yuanguangxin
 */
public interface BeanPostProcessor {
    Object postProcessAfterInitialization(Object bean, String beanName);
}
