package cn.szz.netty.http.server.builder;

import static cn.szz.netty.http.util.CommUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.szz.netty.http.annotation.NettyHttpException404;
import cn.szz.netty.http.annotation.NettyHttpException500;
import cn.szz.netty.http.annotation.NettyHttpExceptionAware;
import cn.szz.netty.http.server.entity.NettyHttpPreflex;

public class NettyHttpExceptionBuilder implements ReflectionsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpExceptionBuilder.class);

    private static NettyHttpPreflex exception404 = null;

    private final static Map<Class<? extends Throwable>, NettyHttpPreflex> exception500Map = new ConcurrentHashMap<>(16);

    @Override
    public void build(Reflections reflections) {
        if (isNull(reflections)) return;
        Set<Class<?>> beanClassSet = reflections.getTypesAnnotatedWith(NettyHttpExceptionAware.class);
        beanClassSet.forEach(beanClass -> {
            logger.info("Exception aware: {}", beanClass.getName());
            Stream.of(beanClass.getMethods()).forEach(method -> {
                NettyHttpException404 nettyHttpException404 = method.getAnnotation(NettyHttpException404.class);
                NettyHttpException500 nettyHttpException500 = method.getAnnotation(NettyHttpException500.class);
                if (isNull(exception404) && notNull(nettyHttpException404)) {
                    exception404 = new NettyHttpPreflex(beanClass, method);
                }
                if (notNull(nettyHttpException500)) {
                    Stream.of(nettyHttpException500.value()).forEach(ex -> {
                        exception500Map.put(ex, new NettyHttpPreflex(beanClass, method));
                    });
                }
            });
        });
    }

    public static NettyHttpPreflex get404() {
        return exception404;
    }

    public static NettyHttpPreflex get500(Class<? extends Throwable> exceptionType) {
        List<Class<? extends Throwable>> matches = new ArrayList<Class<? extends Throwable>>();
        for (Class<? extends Throwable> mappedException : exception500Map.keySet()) {
            if (mappedException.isAssignableFrom(exceptionType)) {
                matches.add(mappedException);
            }
        }
        if (!matches.isEmpty()) {
            Collections.sort(matches, new ExceptionDepthComparator(exceptionType));
            return exception500Map.get(matches.get(0));
        } else {
            return null;
        }
    }
}

class ExceptionDepthComparator implements Comparator<Class<? extends Throwable>> {

    private final Class<? extends Throwable> targetException;

    public ExceptionDepthComparator(Throwable exception) {
        notNull(exception, "Target exception must not be null");
        this.targetException = exception.getClass();
    }

    public ExceptionDepthComparator(Class<? extends Throwable> exceptionType) {
        notNull(exceptionType, "Target exception type must not be null");
        this.targetException = exceptionType;
    }

    @Override
    public int compare(Class<? extends Throwable> o1, Class<? extends Throwable> o2) {
        int depth1 = getDepth(o1, this.targetException, 0);
        int depth2 = getDepth(o2, this.targetException, 0);
        return (depth1 - depth2);
    }

    private int getDepth(Class<?> declaredException, Class<?> exceptionToMatch, int depth) {
        if (exceptionToMatch.equals(declaredException)) {
            return depth;
        }
        if (exceptionToMatch == Throwable.class) {
            return Integer.MAX_VALUE;
        }
        return getDepth(declaredException, exceptionToMatch.getSuperclass(), depth + 1);
    }
}