package group.dump.web;

import group.dump.beans.ApplicationContext;
import group.dump.web.method.model.HandlerMethod;
import group.dump.beans.util.ApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yuanguangxin
 */
public class DumpServletDispatcher extends HttpServlet {

    private HandleMapping handleMapping = new HandleMapping();
    private HandleMethodAdapter handleMethodAdapter = new HandleMethodAdapter();

    @Override
    public final void init() {
        ApplicationContextUtils.refresh();
        handleMapping.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        HandlerMethod handleMethod = handleMapping.getHandler(uri);
        handleMethodAdapter.handle(req, resp, handleMethod);
    }
}
