package com.dump.filter;

import com.dump.bean.BeanFactory;
import com.dump.bean.annotation.Autowired;
import com.dump.filter.annotation.*;
import com.dump.util.FindClassByAnnotationName;
import com.dump.util.PackagePath;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Dump默认过滤器
 */
public class DefaultFilter extends HttpServlet {
    private Set<Class<?>> cons;
    private Set<Class<?>> handles;

    public DefaultFilter() {
    }

    public void init() {
        try {
            this.cons = FindClassByAnnotationName.getClass(PackagePath.getPath(), "com.dump.filter.annotation.Controller");
            this.handles = FindClassByAnnotationName.getClass(PackagePath.getPath(), "com.dump.filter.annotation.Handle");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.indexOf("/"), uri.lastIndexOf("."));
        Iterator it = this.cons.iterator();
        String returnUrl = "";
        boolean b = true;
        if (this.handles.iterator().hasNext()) {
            Class handle = this.handles.iterator().next();
            String[] handleUrl = new String[]{};
            String[] exceptUrl = new String[]{};
            if (handle.getAnnotation(Handle.class) != null) {
                handleUrl = ((Handle) handle.getAnnotation(Handle.class)).value();
            }
            if (handle.getAnnotation(Except.class) != null) {
                exceptUrl = ((Except) handle.getAnnotation(Except.class)).value();
            }
            handle:
            for (int i = 0; i < handleUrl.length; i++) {
                if (uri.matches(handleUrl[i])) {
                    for (int j = 0; j < exceptUrl.length; j++) {
                        if (uri.matches(exceptUrl[j])) break handle;
                    }
                    try {
                        Object ob = handle.newInstance();
                        Method m = handle.getDeclaredMethod("preHandle", HttpServletRequest.class, HttpServletResponse.class);
                        b = (boolean) m.invoke(ob, request, response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        label110:
        if (b == true) {
            while (it.hasNext()) {
                Class dispatcher = (Class) it.next();
                Controller contro = (Controller) dispatcher.getAnnotation(Controller.class);
                String namespace = contro.value();
                Method[] var8 = dispatcher.getDeclaredMethods();
                int var9 = var8.length;
                for (int var10 = 0; var10 < var9; ++var10) {
                    Method method = var8[var10];
                    RequestMapping rm = method.getDeclaredAnnotation(RequestMapping.class);
                    boolean isPar = false;
                    if (rm != null) {
                        for (int i = 0; i < rm.value().length; i++) {
                            if ((namespace + rm.value()[i]).equals(path)) {
                                isPar = true;
                                break;
                            }
                        }
                    }
                    if (rm != null && isPar) {
                        try {
                            Parameter[] e = method.getParameters();
                            Object[] paras = new Object[e.length];

                            for (int o = 0; o < e.length; ++o) {
                                Parameter p = e[o];
                                if (p.getType().equals(HttpServletRequest.class)) {
                                    paras[o] = request;
                                } else if (p.getType().equals(HttpServletResponse.class)) {
                                    paras[o] = response;
                                } else if (p.getType().equals(HttpSession.class)) {
                                    paras[o] = request.getSession();
                                } else if (p.getDeclaredAnnotation(Param.class) != null) {
                                    Param var30 = (Param) p.getDeclaredAnnotations()[0];
                                    paras[o] = request.getParameter(var30.value());
                                    request.setAttribute(var30.value(), request.getParameter(var30.value()));
                                } else {
                                    Class modelClass = p.getType();
                                    Object obj = modelClass.newInstance();
                                    Field[] field = modelClass.getDeclaredFields();
                                    Field[] fName = field;
                                    int ob = field.length;

                                    for (int e1 = 0; e1 < ob; ++e1) {
                                        Field f = fName[e1];
                                        f.setAccessible(true);
                                        String fieldParam = f.getName();
                                        if (f.getType().equals(String.class)) {
                                            f.set(obj, request.getParameter(fieldParam));
                                        } else if (f.getType().equals(Integer.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Integer.valueOf(0));
                                            } else {
                                                f.set(obj, Integer.valueOf(Integer.parseInt(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Double.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Double.valueOf(0.00));
                                            } else {
                                                f.set(obj, Double.valueOf(Double.parseDouble(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Float.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Double.valueOf(0.0f));
                                            } else {
                                                f.set(obj, Float.valueOf(Float.parseFloat(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Boolean.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Boolean.valueOf(false));
                                            } else {
                                                f.set(obj, Boolean.valueOf(Boolean.parseBoolean(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Long.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Long.valueOf(0l));
                                            } else {
                                                f.set(obj, Long.valueOf(Long.parseLong(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Byte.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Byte.valueOf((byte) 0));
                                            } else {
                                                f.set(obj, Byte.valueOf(Byte.parseByte(request.getParameter(fieldParam))));
                                            }
                                        } else if (f.getType().equals(Character.TYPE)) {
                                            if (request.getParameter(fieldParam) == null) {
                                                f.set(obj, Character.valueOf('\u0000'));
                                            } else {
                                                f.set(obj, Character.valueOf(request.getParameter(fieldParam).charAt(0)));
                                            }
                                        }
                                    }

                                    paras[o] = obj;
                                }
                            }

                            Object var28 = dispatcher.newInstance();
                            if (dispatcher.getAnnotation(Autowired.class) != null) {
                                Field[] var29 = dispatcher.getDeclaredFields();
                                int var31 = var29.length;

                                for (int var32 = 0; var32 < var31; ++var32) {
                                    Field var33 = var29[var32];
                                    String var34 = var33.getName();
                                    BeanFactory.getBeanFactory();
                                    Object var35 = BeanFactory.getBean(var34);

                                    try {
                                        var33.setAccessible(true);
                                        var33.set(var28, var35);
                                    } catch (IllegalAccessException var25) {
                                        var25.printStackTrace();
                                    }
                                }
                            }

                            if (method.getReturnType().equals(String.class)) {
                                returnUrl = (String) method.invoke(var28, paras);
                            } else {
                                method.invoke(var28, paras);
                            }
                        } catch (Exception var26) {
                            var26.printStackTrace();
                        }
                        break label110;
                    }
                }
            }
        }
        if (b == true) {
            if (!returnUrl.equals("")) {
                if (returnUrl.split(":")[0].equals("redirect")) {
                    response.sendRedirect(returnUrl.split(":")[1]);
                } else {
                    RequestDispatcher var27 = request.getRequestDispatcher(returnUrl);
                    var27.forward(request, response);
                }
            }
        }
    }
}
