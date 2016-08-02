package com.dump.filter;

import com.dump.filter.util.FindClassByAnnotationName;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DefaultFilter extends HttpServlet{
    private Set<Class<?>> cons;
    @Override
    public void init(){
        System.out.println("init");
        try {
            cons = FindClassByAnnotationName.getClass("test.controller","com.dump.filter.Controller");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.lastIndexOf("/"),uri.lastIndexOf("."));
        Iterator<Class<?>> it = cons.iterator();
        String returnUrl = "";
        findUrl:while (it.hasNext()){
            Class<?> c = it.next();
            for (Method method: c.getDeclaredMethods()){
                Annotation[] anns = method.getDeclaredAnnotations();
                RequestMapping rm = (RequestMapping) anns[0];
                if(rm.value().equals(path)){
                    try {
                        Parameter[] parameters = method.getParameters();
                        Object[] paras = new Object[parameters.length];
                        for (int i=0;i<parameters.length;i++){
                            Parameter p = parameters[i];
                            if(p.getType().equals(HttpServletRequest.class)){
                                paras[i] = request;
                            }else if(p.getType().equals(HttpServletResponse.class)){
                                paras[i] = response;
                            }else if(p.getType().equals(HttpSession.class)){
                                paras[i] = request.getSession();
                            }else {
                                System.out.println(p.isNamePresent());
                                paras[i] = request.getParameter(p.getName());
                            }
                        }
                        Object o = c.newInstance();
                        if(method.getReturnType().equals(String.class)){
                            returnUrl = (String) method.invoke(o,paras);
                        }else {
                            method.invoke(o,paras);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break findUrl;
                }
            }
        }
        if(!returnUrl.equals("")){
            if(returnUrl.split(":")[0].equals("redirect")){
                response.sendRedirect(returnUrl.split(":")[1]);
            }else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(returnUrl);
                dispatcher .forward(request, response);
            }
        }
    }
}
