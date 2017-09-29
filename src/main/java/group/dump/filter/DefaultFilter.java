package group.dump.filter;

import group.dump.bean.BeanFactory;
import group.dump.bean.annotation.Autowired;
import group.dump.filter.annotation.*;
import group.dump.interceptor.annotation.Except;
import group.dump.interceptor.annotation.Handle;
import group.dump.util.AnnotationUtil;
import group.dump.util.LoadConfig;

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
            this.cons = AnnotationUtil.getClass(LoadConfig.getPath(), "group.dump.filter.annotation.Controller");
            this.handles = AnnotationUtil.getClass(LoadConfig.getPath(), "group.dump.interceptor.annotation.Handle");
        } catch (Exception e) {
            e.printStackTrace();
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
        label:
        if (b == true) {
            while (it.hasNext()) {
                Class dispatcher = (Class) it.next();
                Controller contro = (Controller) dispatcher.getAnnotation(Controller.class);
                String[] namespace = contro.value();
                Method[] meths = dispatcher.getDeclaredMethods();
                int len = meths.length;
                for (int i = 0; i < len; ++i) {
                    Method method = meths[i];
                    RequestMapping rm = method.getDeclaredAnnotation(RequestMapping.class);
                    boolean isPar = false;
                    if (rm != null) {
                        for (int n = 0; n < namespace.length; n++) {
                            for (int m = 0; m < rm.value().length; m++) {
                                if ((namespace[n] + rm.value()[m]).equals(path)) {
                                    isPar = true;
                                    break;
                                }
                            }
                            if (isPar) break;
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
                                    Param par = (Param) p.getDeclaredAnnotations()[0];
                                    paras[o] = request.getParameter(par.value());
                                    request.setAttribute(par.value(), request.getParameter(par.value()));
                                } else {
                                    Class modelClass = p.getType();
                                    Object obj = modelClass.newInstance();
                                    Field[] field = modelClass.getDeclaredFields();
                                    Field[] fName = field;
                                    int ob = field.length;

                                    for (int z = 0; z < ob; ++z) {
                                        Field f = fName[z];
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

                            Object ob = dispatcher.newInstance();
                            if (dispatcher.getAnnotation(Autowired.class) != null) {
                                Field[] fies = dispatcher.getDeclaredFields();
                                int flen = fies.length;

                                for (int k = 0; k < flen; ++k) {
                                    Field f = fies[k];
                                    String str = f.getName();
                                    BeanFactory.getBeanFactory();
                                    Object obj = BeanFactory.getBean(str);
                                    try {
                                        f.setAccessible(true);
                                        f.set(ob, obj);
                                    } catch (IllegalAccessException ae) {
                                        ae.printStackTrace();
                                    }
                                }
                            }

                            if (method.getReturnType().equals(String.class)) {
                                returnUrl = (String) method.invoke(ob, paras);
                            } else {
                                method.invoke(ob, paras);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break label;
                    }
                }
            }
        }
        if (b == true) {
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
}
