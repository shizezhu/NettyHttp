package cn.szz.netty.http.server.handler;

import static cn.szz.netty.http.util.CommUtils.*;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import cn.szz.netty.http.server.entity.NettyHttpPreflex;

public class NettyHttpException500Handler implements NettyHttpPreflexHandler {

    private Throwable throwable;

    public NettyHttpException500Handler(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Object handle(NettyHttpPreflex nettyHttpPreflex) throws Exception {
        if (isNull(nettyHttpPreflex)) return "500, Server exception";
        return nettyHttpPreflex.invoke(getParameters(nettyHttpPreflex.getParameters()));
    }

    protected Object[] getParameters(Parameter[] parameters) {
        if (isNull(parameters)) return new Object[] {};
        return Stream.of(parameters).map(parameter -> getParameter(parameter)).toArray();
    }

    protected Object getParameter(Parameter parameter) {
        if (isNull(parameter)) return null;
        Class<?> type = parameter.getType();
        if (type.isAssignableFrom(throwable.getClass())) {
            return throwable;
        } else {
            return null;
        }
    }

}
