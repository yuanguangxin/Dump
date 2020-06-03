package group.dump.web.method.support.impl;

import group.dump.web.annotation.RequestModel;
import group.dump.exception.DumpException;
import group.dump.web.method.convert.RequestParameterConverter;
import group.dump.web.method.support.HandlerMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * @author yuanguangxin
 */
public class RequestModelResolver implements HandlerMethodArgumentResolver {
    @Override
    public Boolean support(Parameter parameter) {
        return parameter.getAnnotation(RequestModel.class) != null;
    }

    @Override
    public Object resolveArgument(HttpServletRequest request, Class<?> requiredType, Parameter parameter) {
        Class<?> targetClass = parameter.getType();
        Object obj;
        try {
            obj = targetClass.newInstance();
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = request.getParameter(field.getName());
                if (value == null) {
                    field.set(obj, null);
                    continue;
                }
                RequestParameterConverter converter = new RequestParameterConverter();
                value = converter.convert(field.getType(), value);
                field.set(obj, value);
            }
        } catch (Exception ex) {
            throw new DumpException("can not create instance for '" + targetClass.getName() + "'", ex);
        }
        return obj;
    }
}
