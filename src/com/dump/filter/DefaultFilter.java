package com.dump.filter;

import com.dump.bean.BeanFactory;
import com.dump.filter.annotation.Param;
import com.dump.filter.annotation.RequestMapping;
import com.dump.util.FindClassByAnnotationName;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Dump默认过滤器
 */
public class DefaultFilter extends HttpServlet {
    private Set<Class<?>> cons;

    @Override
    public void init() {
        try {
            cons = FindClassByAnnotationName.getClass("test.controller", "com.dump.filter.annotation.Controller");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.lastIndexOf("/"), uri.lastIndexOf("."));
        Iterator<Class<?>> it = cons.iterator();
        String returnUrl = "";
        findUrl:
        while (it.hasNext()) {
            Class<?> c = it.next();
            for (Method method : c.getDeclaredMethods()) {
                RequestMapping rm = method.getDeclaredAnnotation(RequestMapping.class);
                if(rm!=null){
                    if (rm.value().equals(path)) {
                        try {
                            Parameter[] parameters = method.getParameters();
                            Object[] paras = new Object[parameters.length];
                            for (int i = 0; i < parameters.length; i++) {
                                Parameter p = parameters[i];
                                if (p.getType().equals(HttpServletRequest.class)) {
                                    paras[i] = request;
                                } else if (p.getType().equals(HttpServletResponse.class)) {
                                    paras[i] = response;
                                } else if (p.getType().equals(HttpSession.class)) {
                                    paras[i] = request.getSession();
                                } else {
                                    if (p.getDeclaredAnnotation(Param.class) != null) {
                                        Param param = (Param) p.getDeclaredAnnotations()[0];
                                        paras[i] = request.getParameter(param.value());
                                        request.setAttribute(param.value(), request.getParameter(param.value()));
                                    } else {
                                        Class modelClass = p.getType();
                                        Object obj = modelClass.newInstance();
                                        Field[] fields = modelClass.getDeclaredFields();
                                        for (Field f : fields) {
                                            f.setAccessible(true);
                                            String fieldParam = f.getName();
                                            if (f.getType().equals(String.class)) {
                                                f.set(obj, request.getParameter(fieldParam));
                                            } else if (f.getType().equals(int.class)) {
                                                if (request.getParameter(fieldParam) != "") {
                                                    f.set(obj, Integer.parseInt(request.getParameter(fieldParam)));
                                                } else {
                                                    f.set(obj, 0);
                                                }
                                            }
                                        }
                                        paras[i] = obj;
                                    }
                                }
                            }
                            Object o = c.newInstance();
                            for (Field field:c.getDeclaredFields()){
                                String fName = field.getName();
                                Object ob = BeanFactory.getBeanFactory().getBean(fName);
                                try {
                                    field.setAccessible(true);
                                    field.set(o,ob);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (method.getReturnType().equals(String.class)) {
                                returnUrl = (String) method.invoke(o, paras);
                            } else {
                                method.invoke(o, paras);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break findUrl;
                    }
                }
            }
        }
        if (!returnUrl.equals("")) {
            if (returnUrl.split(":")[0].equals("redirect")) {
                response.sendRedirect(returnUrl.split(":")[1]);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(returnUrl);
                dispatcher.forward(request, response);
            }
        }
    }
}
