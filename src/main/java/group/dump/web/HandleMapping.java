package group.dump.web;

import group.dump.beans.util.ApplicationContextUtils;
import group.dump.util.ReflectionUtils;
import group.dump.web.annotation.Controller;
import group.dump.web.annotation.RequestMapping;
import group.dump.exception.DumpException;
import group.dump.web.method.model.HandlerMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanguangxin
 */
public class HandleMapping {
    private Map<String, HandlerMethod> mappings = new HashMap<>();

    public void init() {
        Set<Class<?>> controllerSet = ReflectionUtils.getAllClass(Controller.class);
        controllerSet.forEach((controller) -> {
            RequestMapping requestMappingAnnotation = controller.getAnnotation(RequestMapping.class);
            if (requestMappingAnnotation == null) {
                throw new DumpException("controller '" + controller.getName() + "' must have a '@RequestMapping' annotation");
            }
            String parentPath = requestMappingAnnotation.value();
            Method[] methods = controller.getMethods();
            for (Method method : methods) {
                RequestMapping methodRequestMappingAnnotation = method.getAnnotation(RequestMapping.class);
                if (methodRequestMappingAnnotation == null) {
                    continue;
                }
                String path = methodRequestMappingAnnotation.value();
                try {
                    mappings.put(parentPath + path, new HandlerMethod(ApplicationContextUtils.getContext().getBean(controller), method));
                } catch (Exception e) {
                    throw new DumpException("init controller failed,can not create instance for controller '" + controller.getName() + "'", e);
                }
            }
        });
    }

    public HandlerMethod getHandler(String url) {
        HandlerMethod handleMethod = mappings.get(url);
        if (handleMethod == null) {
            throw new DumpException("path '" + url + "' can not find handle");
        }
        return handleMethod;
    }
}
