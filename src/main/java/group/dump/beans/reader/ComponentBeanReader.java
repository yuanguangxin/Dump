package group.dump.beans.reader;

import group.dump.beans.annotation.Component;
import group.dump.beans.model.BeanDefinition;
import group.dump.beans.support.impl.DefaultBeanFactory;
import group.dump.util.StringUtils;
import group.dump.util.ReflectionUtils;
import group.dump.web.annotation.Controller;

import java.util.Set;

/**
 * @author yuanguangxin
 */
public class ComponentBeanReader {

    public void readBeanDefinition(DefaultBeanFactory beanFactory) {
        Set<Class<?>> componentSet = ReflectionUtils.getAllClass(Component.class);
        Set<Class<?>> controllerSet = ReflectionUtils.getAllClass(Controller.class);
        componentSet.addAll(controllerSet);
        componentSet.forEach((componentClass) -> {
            BeanDefinition beanDefinition = new BeanDefinition();
            String beanName = componentClass.getAnnotation(Component.class) != null ? componentClass.getAnnotation(Component.class).value() : componentClass.getAnnotation(Controller.class).value();
            if ("".equals(beanName)) {
                beanName = StringUtils.lowerFirst(componentClass.getSimpleName());
            }
            beanDefinition.setBeanClass(componentClass);
            beanFactory.registerBeanDefinition(beanName, beanDefinition);
        });
    }
}
