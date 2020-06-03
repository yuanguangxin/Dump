package group.dump.beans.support.impl;

import group.dump.beans.annotation.Resource;
import group.dump.beans.model.BeanDefinition;
import group.dump.beans.processor.BeanPostProcessor;
import group.dump.beans.support.BeanFactory;
import group.dump.beans.support.SingletonBeanRegistry;
import group.dump.beans.util.BeanUtils;
import group.dump.util.StringUtils;
import group.dump.exception.DumpException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuanguangxin
 */
public class DefaultBeanFactory extends SingletonBeanRegistry implements BeanFactory {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public void preInstantiateSingletons() {
        this.beanDefinitionMap.forEach((beanName, beanDef) -> {
            getBean(beanName);
        });
    }

    @Override
    public Object getBean(String name) {
        return doGetBean(name);
    }

    @SuppressWarnings("unchecked")
    private <T> T doGetBean(String beanName) {
        Object bean;
        Object sharedInstance = getSingleton(beanName, true);
        if (sharedInstance != null) {
            bean = sharedInstance;
        } else {
            BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
            if (beanDefinition == null) {
                throw new DumpException("can not find the definition of bean '" + beanName + "'");
            }
            bean = getSingleton(beanName, () -> {
                try {
                    return doCreateBean(beanName, beanDefinition);
                } catch (Exception ex) {
                    removeSingleton(beanName);
                    throw ex;
                }
            });
        }
        return (T) bean;
    }

    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = createBeanInstance(beanName, beanDefinition);
        boolean earlySingletonExposure = isSingletonCurrentlyInCreation(beanName);
        if (earlySingletonExposure) {
            addSingletonFactory(beanName, () -> bean);
        }
        Object exposedObject = bean;
        populateBean(beanName, beanDefinition, bean);
//        exposedObject = initializeBean(exposedObject, beanName);
        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
//                if (exposedObject == bean) {
                exposedObject = earlySingletonReference;
//                }
            }
        }
        return exposedObject;
    }

    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Constructor<?> constructorToUse;
        if (beanClass.isInterface()) {
            throw new DumpException("Specified class '" + beanName + "' is an interface");
        }
        try {
            constructorToUse = beanClass.getDeclaredConstructor((Class<?>[]) null);
            return BeanUtils.instantiateClass(constructorToUse);
        } catch (Exception e) {
            throw new DumpException("'" + beanName + "',No default constructor found", e);
        }
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, Object beanInstance) {
        Field[] beanFields = beanDefinition.getBeanClass().getDeclaredFields();
        try {
            for (Field field : beanFields) {
                if (field.getAnnotation(Resource.class) == null) {
                    continue;
                }
                if (!containsBean(field.getName())) {
                    throw new DumpException("'@Resource' for field '" + field.getClass().getName() + "' can not find");
                }
                field.setAccessible(true);
                field.set(beanInstance, getBean(field.getName()));
            }
        } catch (Exception e) {
            throw new DumpException("populateBean '" + beanName + "' error", e);
        }
    }

//    private Object initializeBean(Object bean, String beanName) {
//        for (BeanPostProcessor beanProcessor : this.beanPostProcessors) {
//            bean = beanProcessor.postProcessAfterInitialization(bean, beanName);
//        }
//        return bean;
//    }

    private boolean containsBeanDefinition(String name) {
        return beanDefinitionMap.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) getBean(StringUtils.lowerFirst(requiredType.getSimpleName()));
    }

    @Override
    public boolean containsBean(String name) {
        return this.containsSingleton(name) || this.containsBeanDefinition(name);
    }
}
