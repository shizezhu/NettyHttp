package cn.szz.netty.http.server.handler;

import static cn.szz.netty.http.util.CommUtils.isEmpty;
import static cn.szz.netty.http.util.CommUtils.isNull;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import cn.szz.netty.http.annotation.NettyHttpParam;
import cn.szz.netty.http.server.entity.NettyHttpFile;
import cn.szz.netty.http.server.entity.NettyHttpPreflex;
import cn.szz.netty.http.server.request.DefaultNettyHttpRequest;
import cn.szz.netty.http.server.request.NettyHttpRequest;
import cn.szz.netty.http.server.response.DefaultNettyHttpResponse;
import cn.szz.netty.http.server.response.NettyHttpResponse;

public class NettyHttpMappingHandler implements NettyHttpPreflexHandler {

    private NettyHttpRequest request;
    private NettyHttpResponse response;

    public NettyHttpMappingHandler(NettyHttpRequest request, NettyHttpResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public Object handle(NettyHttpPreflex nettyHttpPreflex) throws Exception {
        if (isNull(nettyHttpPreflex)) return null;
        return nettyHttpPreflex.invoke(getParameters(nettyHttpPreflex.getParameters()));
    }

    protected Object[] getParameters(Parameter[] parameters) {
        if (isNull(parameters)) return new Object[] {};
        return Stream.of(parameters).map(parameter -> getParameter(parameter)).toArray();
    }

    protected Object getParameter(Parameter parameter) {
        if (isNull(parameter)) return null;
        Class<?> type = parameter.getType();
        if (NettyHttpRequest.class.equals(type) || DefaultNettyHttpRequest.class.equals(type)) {
            return request;
        } else if (NettyHttpResponse.class.equals(type) || DefaultNettyHttpResponse.class.equals(type)) {
            return response;
        } else if (Map.class.equals(type) || HashMap.class.equals(type)) {
            return getMap(parameter);
        } else {
            return getOther(parameter);
        }
    }

    protected Object getMap(Parameter parameter) {
        if (isNull(request) || isNull(parameter)) return null;
        Map<String, List<String>> parameterMap = request.getParameterMap();
        Type mapType = parameter.getParameterizedType();
        if (mapType instanceof ParameterizedType) {
            ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
            Type[] mapActualTypeArguments = mapParameterizedType.getActualTypeArguments();
            String mapActualTypeName0 = mapActualTypeArguments[0].getTypeName();
            String mapActualTypeName1 = mapActualTypeArguments[1].getTypeName();
            if ("java.lang.String".equals(mapActualTypeName0)
                    && "java.lang.String".equals(mapActualTypeName1)) {
                Map<String, String> result = new LinkedHashMap<String, String>(parameterMap.size());
                for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
                    if (entry.getValue().size() > 0) {
                        result.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
                return result;
            }
            if ("java.lang.String".equals(mapActualTypeName0)
                    && ("java.util.List<java.lang.String>".equals(mapActualTypeName1)
                            || "java.util.ArrayList<java.lang.String>".equals(mapActualTypeName1))) {
                return parameterMap;
            }
            return null;
        }
        return parameterMap;
    }

    protected Object getOther(Parameter parameter) {
        if (isNull(request) || isNull(parameter)) return null;
        NettyHttpParam nettyHttpParam = parameter.getAnnotation(NettyHttpParam.class);
        Class<?> type = parameter.getType();
        String typeName = type.getTypeName();
        if (type.isPrimitive()) {
            if (isNull(nettyHttpParam))
                throw new IllegalStateException(
                        String.format("Primitive type %s can't be null", typeName));
            String name = nettyHttpParam.value();
            String value = request.getParameter(name);
            if (isEmpty(value))
                throw new IllegalStateException(
                        String.format("Parameter '%s' of primitive type %s cannot be null", name, typeName));
            try {
                return getPrimitive(typeName, value);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("Input '%s' can't be converted to the parameter %s of primitive type %s", value, name, typeName));
            }
        } else {
            if (isNull(nettyHttpParam))
                return null;
            String name = nettyHttpParam.value();
            if (NettyHttpFile.class.equals(type))
                return request.getFile(name);
            String value = request.getParameter(name);
            if (isEmpty(value))
                return null;
            try {
                return type.getConstructor(String.class).newInstance(value);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("Input '%s' can't be converted to the parameter %s of primitive type %s", value, name, typeName));
            }
        }
    }

    protected Object getPrimitive(String typeName, String value) {
        switch (typeName) {
        case "byte":
            return Byte.parseByte(value);
        case "char":
            return value.toCharArray()[0];
        case "short":
            return Short.parseShort(value);
        case "int":
            return Integer.parseInt(value);
        case "float":
            return Float.parseFloat(value);
        case "long":
            return Long.parseLong(value);
        case "double":
            return Double.parseDouble(value);
        case "boolean":
            return Boolean.parseBoolean(value);
        default:
            throw new IllegalStateException();
        }
    }
}
