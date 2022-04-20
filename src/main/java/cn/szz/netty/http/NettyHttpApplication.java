package cn.szz.netty.http;

import static cn.szz.netty.http.util.CommUtils.ifIsEmptyGet;
import static cn.szz.netty.http.util.CommUtils.isEmpty;
import static cn.szz.netty.http.util.CommUtils.isNull;
import static cn.szz.netty.http.util.CommUtils.notEmpty;
import static cn.szz.netty.http.util.CommUtils.notInt;
import static cn.szz.netty.http.util.CommUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.szz.netty.http.annotation.NettyHttpServerApplication;
import cn.szz.netty.http.server.NettyHttpServer;
import cn.szz.netty.http.server.builder.NettyHttpExceptionBuilder;
import cn.szz.netty.http.server.builder.NettyHttpFilterBuilder;
import cn.szz.netty.http.server.builder.NettyHttpUriMapperBuilder;
import cn.szz.netty.http.server.builder.ReflectionsBuilder;

/**
 * Http主方法
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:59:29
 */
public class NettyHttpApplication {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpApplication.class);

    protected static final int DEFAULT_SERVER_PORT = 8080;
    protected static final int DEFAULT_SERVER_BACKLOG = 128;
    protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    protected String[] packages;
    protected String serverHost;
    protected int serverPort;
    protected String serverPath;
    protected int serverBacklog;
    protected String dateFormat;

    public NettyHttpApplication(Class<?> application, List<String> args) {
        NettyHttpServerApplication app = parseApplication(application);
        Map<String, String> sysProps = parseSysProps();
        Map<String, String> argProps = parseArgProps(args);
        this.packages = parsePackages(app, sysProps, argProps);
        this.serverHost = parseServerHost(app, sysProps, argProps);
        this.serverPort = parseServerPort(app, sysProps, argProps);
        this.serverPath = parseServerPath(app, sysProps, argProps);
        this.serverBacklog = parseServerBacklog(app, sysProps, argProps);
        this.dateFormat = parseDateFormat(app, sysProps, argProps);
        Reflections reflections = getReflections(getPackages());
        getReflectionsBuilderList().forEach(reflectionsBuilder -> reflectionsBuilder.build(reflections));
        NettyHttpServer.run(getServerHost(), getServerPort(), getServerBacklog(), getDateFormat());
    }

    public static NettyHttpApplication run(Class<?> application, String[] args) {
        return run(application, args == null ? new ArrayList<>() : Arrays.asList(args));
    }

    public static NettyHttpApplication run(Class<?> application, List<String> args) {
        return new NettyHttpApplication(application, args);
    }

    protected NettyHttpServerApplication parseApplication(Class<?> application) {
        return notNull(notNull(application, "application").getAnnotation(NettyHttpServerApplication.class), "not found NettyHttpServerApplication annotation");
    }

    protected Map<String, String> parseSysProps() {
        Properties sysProps = System.getProperties();
        logger.info("System props: " + sysProps);
        Map<String, String> result = new HashMap<>();
        sysProps.entrySet().forEach(entry -> {
            if (isNull(entry.getKey()))
                return;
            String key = String.valueOf(entry.getKey()).trim();
            if (isEmpty(key))
                return;
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getKey()).trim();
            result.put(key, value);
        });
        return result;
    }

    protected Map<String, String> parseArgProps(List<String> argList) {
        logger.info("Arg props: " + argList);
        Map<String, String> result = new HashMap<>();
        if (argList == null)
            return result;
        argList.forEach(arg -> {
            if (isEmpty(arg) || !arg.contains("="))
                return;
            String key = arg.substring(0, arg.indexOf("=")).trim();
            if (isEmpty(key))
                return;
            String value = arg.substring(arg.indexOf("=") + 1, arg.length()).trim();
            result.put(key, value);
        });
        return result;
    }

    protected String[] parsePackages(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        return application.packages();
    }

    protected String parseServerHost(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        String host = "";
        if (sysProps.containsKey("server.host")) {
            host = sysProps.get("server.host");
        } else if (argProps.containsKey("server.host")) {
            host = argProps.get("server.host");
        } else {
            host = application.serverHost();
        }
        return ifIsEmptyGet(host, "");
    }

    protected int parseServerPort(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        if (sysProps.containsKey("server.port")) {
            return notInt(sysProps.get("server.port"), "server.port不正确: 配置来源: System");
        } else if (argProps.containsKey("server.port")) {
            return notInt(argProps.get("server.port"), "server.port不正确: 配置来源: Args");
        } else if (notEmpty(application.serverPort())) {
            return notInt(application.serverPort(), "server.port不正确: 配置来源: Application");
        } else {
            return DEFAULT_SERVER_PORT;
        }
    }

    protected String parseServerPath(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        String path = "";
        if (sysProps.containsKey("server.path")) {
            path = sysProps.get("server.path");
        } else if (argProps.containsKey("server.path")) {
            path = argProps.get("server.path");
        } else {
            path = application.serverPath();
        }
        return ifIsEmptyGet(path, "");
    }

    protected int parseServerBacklog(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        if (sysProps.containsKey("server.backlog")) {
            return notInt(sysProps.get("server.backlog"), "server.backlog不正确: 配置来源: System");
        } else if (argProps.containsKey("server.backlog")) {
            return notInt(argProps.get("server.backlog"), "server.backlog不正确: 配置来源: Args");
        } else if (notEmpty(application.serverBacklog())) {
            return notInt(application.serverBacklog(), "server.backlog不正确: 配置来源: Application");
        } else {
            return DEFAULT_SERVER_BACKLOG;
        }
    }

    protected String parseDateFormat(
            NettyHttpServerApplication application, Map<String, String> sysProps, Map<String, String> argProps) {
        if (sysProps.containsKey("date.format")) {
            return notEmpty(sysProps.get("date.format"), "date.format不能为空: 配置来源: System");
        } else if (argProps.containsKey("date.format")) {
            return notEmpty(argProps.get("date.format"), "date.format不能为空: 配置来源: Args");
        } else if (notEmpty(application.dateFormat())) {
            return notEmpty(application.dateFormat(), "date.format不能为空: 配置来源: Application");
        } else {
            return DEFAULT_DATE_FORMAT;
        }
    }

    protected Reflections getReflections(Object[] packages) {
        if (isNull(packages) || packages.length == 0)
            return null;
        return new Reflections(packages);
    }

    protected List<ReflectionsBuilder> getReflectionsBuilderList() {
        List<ReflectionsBuilder> reflectionsBuilderList = new ArrayList<>();
        reflectionsBuilderList.add(new NettyHttpUriMapperBuilder(getServerPath()));
        reflectionsBuilderList.add(new NettyHttpExceptionBuilder());
        reflectionsBuilderList.add(new NettyHttpFilterBuilder());
        return reflectionsBuilderList;
    }

    public String[] getPackages() {
        return packages;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerPath() {
        return serverPath;
    }

    public int getServerBacklog() {
        return serverBacklog;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}