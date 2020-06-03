package group.dump.web.method.support.impl;

import group.dump.web.annotation.RequestParam;
import group.dump.web.constants.ValueConstants;
import group.dump.exception.DumpException;
import group.dump.web.method.convert.RequestParameterConverter;
import group.dump.web.method.model.NamedValueInfo;
import group.dump.web.method.support.HandlerMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

/**
 * @author yuanguangxin
 */
public class RequestParamResolver implements HandlerMethodArgumentResolver {
    @Override
    public Boolean support(Parameter parameter) {
        return parameter.getAnnotation(RequestParam.class) != null;
    }

    @Override
    public Object resolveArgument(HttpServletRequest request, Class<?> requiredType, Parameter parameter) {
        NamedValueInfo namedValueInfo = buildNamedValueInfo(parameter);
        String value = request.getParameter(namedValueInfo.getName());
        if (value == null) {
            if (namedValueInfo.isRequired() && namedValueInfo.getDefaultValue() == null) {
                throw new DumpException("RequestParam for '" + namedValueInfo.getName() + "' value can not be null");
            }
            value = namedValueInfo.getDefaultValue();
        }
        RequestParameterConverter converter = new RequestParameterConverter();
        return converter.convert(requiredType, value);
    }

    private NamedValueInfo buildNamedValueInfo(Parameter parameter) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        String defaultValue = requestParam.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? null : requestParam.defaultValue();
        NamedValueInfo valueInfo = new NamedValueInfo(requestParam.value(), requestParam.required(), defaultValue);
        return valueInfo;
    }
}
