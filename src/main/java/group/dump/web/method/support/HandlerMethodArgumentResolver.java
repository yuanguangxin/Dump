package group.dump.web.method.support;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

/**
 * @author yuanguangxin
 */
public interface HandlerMethodArgumentResolver {

    Boolean support(Parameter parameter);

    Object resolveArgument(HttpServletRequest request, Class<?> requiredType, Parameter parameter);
}
