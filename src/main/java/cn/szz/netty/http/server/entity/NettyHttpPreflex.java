package cn.szz.netty.http.server.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class NettyHttpPreflex {

    private Class<?> type;

    private Method method;

    public NettyHttpPreflex(Class<?> type, Method method) {
        this.type = type;
        this.method = method;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Parameter[] getParameters() {
        return this.getMethod().getParameters();
    }

    public Object invoke(Object[] parameters) throws Exception {
        return this.getMethod().invoke(this.getType().newInstance(), parameters);
    }
}
