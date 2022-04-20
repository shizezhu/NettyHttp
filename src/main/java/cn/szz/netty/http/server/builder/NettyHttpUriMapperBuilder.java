package cn.szz.netty.http.server.builder;

import static cn.szz.netty.http.util.CommUtils.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.szz.netty.http.annotation.NettyHttpController;
import cn.szz.netty.http.annotation.NettyHttpMapping;
import cn.szz.netty.http.server.entity.NettyHttpPreflex;
import cn.szz.netty.http.util.CommUtils;

public class NettyHttpUriMapperBuilder implements ReflectionsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpUriMapperBuilder.class);

    private static Map<String, NettyHttpPreflex> mappingMap = new ConcurrentHashMap<>(16);

    private String path;

    public NettyHttpUriMapperBuilder(String path) {
        this.path = path;
    }

    @Override
    public void build(Reflections reflections) {
        mappingMap.clear();
        if (isNull(reflections)) return;
        String path = handleMapping(getPath());
        Set<Class<?>> beanClassSet = reflections.getTypesAnnotatedWith(NettyHttpController.class);
        beanClassSet.forEach(beanClass -> {
            logger.info("Request Controller: {}", beanClass.getName());
            NettyHttpMapping typeMappingAnn = beanClass.getAnnotation(NettyHttpMapping.class);
            String typeMapping = isNull(typeMappingAnn) ? "" : handleMapping(typeMappingAnn.value());
            Stream.of(beanClass.getMethods()).forEach(method -> {
                NettyHttpMapping methodMappingAnn = method.getAnnotation(NettyHttpMapping.class);
                if (isNull(methodMappingAnn)) return;
                String methodMapping = handleMapping(methodMappingAnn.value());
                String uri = path + typeMapping + methodMapping;
                if (mappingMap.containsKey(uri))
                    throw new IllegalStateException(
                            String.format("Cannot map '%s' method %s to %s: There is already '%s' bean method",
                                    beanClass.getName(), method.getName(), uri, mappingMap.get(uri).getType().getName()));
                mappingMap.put(uri, new NettyHttpPreflex(beanClass, method));
                logger.info("Request mapping: {}", uri);
            });
        });
    }

    public static NettyHttpPreflex getMapping(String uri) {
        if (isEmpty(uri)) return null;
        return mappingMap.get(uri);
    }

    private String handleMapping(String mapping) {
        String result = Stream.of(ifIsEmptyGet(mapping, "").split("/")).filter(CommUtils::notEmpty).map(String::trim).collect(Collectors.joining("/"));
        if (CommUtils.notEmpty(result)) result = "/" + result;
        return result;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
