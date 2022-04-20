package cn.szz.netty.http.server.builder;

import static cn.szz.netty.http.util.CommUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.szz.netty.http.server.entity.NettyHttpFilter;
import cn.szz.netty.http.util.CommUtils;

public class NettyHttpFilterBuilder implements ReflectionsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpFilterBuilder.class);

    private static final List<NettyHttpFilter> filterList = new ArrayList<>();

    @Override
    public void build(Reflections reflections) {
        if (isNull(reflections)) return;
        Set<Class<? extends NettyHttpFilter>> beanClassSet = reflections.getSubTypesOf(NettyHttpFilter.class);
        beanClassSet.stream().map(beanClass -> {
            try {
                return beanClass.newInstance();
            } catch (Exception e) {
                return null;
            }
        }).filter(CommUtils::notNull)
                .sorted((v1, v2) -> new Integer(v1.order()).compareTo(v2.order()))
                .forEach(v -> {
                    filterList.add(v);
                    logger.info("Request filter: {}: {}", v.order(), v.getClass().getName());
                });
    }

    public static List<NettyHttpFilter> getFilterList() {
        return filterList;
    }

}
