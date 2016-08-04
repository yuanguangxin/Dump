package com.dump.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class ProxyInstance {

    public static Object getAuthInstanceByFilter(Class<?> clazz, DumpProxy myProxy){
        Enhancer en = new Enhancer();
        en.setSuperclass(clazz);
        en.setCallbacks(new Callback[]{myProxy, NoOp.INSTANCE});
        en.setCallbackFilter(new ProxyFilter());
        return en.create();
    }
}
