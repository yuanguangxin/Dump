package group.dump.interceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Dump拦截器接口
 */
public interface Interceptor {
    boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
