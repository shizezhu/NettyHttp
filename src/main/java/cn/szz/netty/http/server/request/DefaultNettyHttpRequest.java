package cn.szz.netty.http.server.request;

import static cn.szz.netty.http.util.CommUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.szz.netty.http.server.entity.NettyHttpFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

/**
 * 请求对象
 *
 * @author Shi Zezhu
 * @date 2019年8月2日 下午4:36:28
 */
public class DefaultNettyHttpRequest implements NettyHttpRequest {

    protected String uri;
    protected String requestIp;
    protected int requestPort;
    protected String method;
    protected byte[] byteData;
    protected List<Cookie> cookieList = new ArrayList<>();
    protected Map<String, List<String>> headerMap = new HashMap<>();
    protected Map<String, List<String>> parameterMap = new HashMap<>();
    protected Map<String, List<NettyHttpFile>> fileMap = new HashMap<>();

    public DefaultNettyHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fhr) {
        init(ctx, fhr);
    }

    private void init(ChannelHandlerContext ctx, FullHttpRequest fhr) {
        notNull(ctx, "ctx");
        notNull(fhr, "fhr");
        initUri(fhr);
        initHeader(fhr);
        initCookie(fhr);
        initByteData(fhr);
        initParameter(fhr);
        initAddress(ctx);
        initMethod(fhr);
        if (fhr.refCnt() > 1) fhr.release();
    }

    private void initHeader(FullHttpRequest fhr) {
        fhr.headers().forEach(entry -> {
            if (headerMap.containsKey(entry.getKey())) {
                headerMap.get(entry.getKey()).add(entry.getValue());
            } else {
                headerMap.put(entry.getKey(), Stream.of(entry.getValue()).collect(Collectors.toList()));
            }
        });
    }

    private void initCookie(FullHttpRequest fhr) {
        String cookieStr = fhr.headers().get("Cookie");
        if (notEmpty(cookieStr))
            ServerCookieDecoder.STRICT.decode(cookieStr).forEach(cookieList::add);
    }

    private void initByteData(FullHttpRequest fhr) {
        if (HttpMethod.POST.equals(fhr.method()))
            byteData = byteBufToBytes(fhr.content());
    }

    private void initParameter(FullHttpRequest fhr) {
        if (HttpMethod.GET.equals(fhr.method())) {
            parameterMap = new QueryStringDecoder(fhr.uri()).parameters();
        } else if (HttpMethod.POST.equals(fhr.method())) {
            new HttpPostRequestDecoder(fhr).getBodyHttpDatas().forEach(httpData -> {
                if (HttpDataType.Attribute.equals(httpData.getHttpDataType())) {
                    Attribute data = (Attribute) httpData;
                    try {
                        if (parameterMap.containsKey(data.getName())) {
                            parameterMap.get(data.getName()).add(data.getValue());
                        } else {
                            parameterMap.put(data.getName(), Stream.of(data.getValue()).collect(Collectors.toList()));
                        }
                    } catch (Exception e) {
                        return;
                    } finally {
                        data.delete();
                    }
                } else if (HttpDataType.FileUpload.equals(httpData.getHttpDataType())) {
                    FileUpload data = (FileUpload) httpData;
                    try {
                        if (fileMap.containsKey(data.getName())) {
                            fileMap.get(data.getName()).add(NettyHttpFile.getInstance(data));
                        } else {
                            fileMap.put(data.getName(), Stream.of(NettyHttpFile.getInstance(data)).collect(Collectors.toList()));
                        }
                    } catch (Exception e) {
                        return;
                    } finally {
                        data.delete();
                    }
                }
            });
        }
    }

    private void initUri(FullHttpRequest fhr) {
        uri = fhr.uri().substring(0, fhr.uri().indexOf('?') == -1 ? fhr.uri().length() : fhr.uri().indexOf('?'));
    }

    private void initAddress(ChannelHandlerContext ctx) {
        String address = ctx.channel().remoteAddress().toString().replace("/", "");
        requestIp = address.substring(0, address.lastIndexOf(':'));
        requestPort = Integer.parseInt(address.substring(address.lastIndexOf(':') + 1, address.length()));
    }

    private void initMethod(FullHttpRequest fhr) {
        method = fhr.method().name();
    }

    @Override
    public byte[] getByteData() {
        return byteData;
    }

    @Override
    public String getParameter(String name) {
        if (isEmpty(name)
                || !parameterMap.containsKey(name)
                || parameterMap.get(name).isEmpty())
            return null;
        return parameterMap.get(name).get(0);
    }

    @Override
    public NettyHttpFile getFile(String name) {
        if (isEmpty(name)
                || !fileMap.containsKey(name)
                || fileMap.get(name).isEmpty())
            return null;
        return fileMap.get(name).get(0);
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Map<String, List<NettyHttpFile>> getFileMap() {
        return fileMap;
    }

    @Override
    public List<String> getParameterNames() {
        return Stream.of(parameterMap.keySet(), fileMap.keySet())
                .flatMap(Set::stream).collect(Collectors.toList());
    }

    @Override
    public String getCookieValue(String name) {
        Cookie cookie = this.getCookie(name);
        if (isNull(cookie))
            return null;
        return cookie.value();
    }

    @Override
    public Cookie getCookie(String name) {
        if (isEmpty(name))
            return null;
        for (Cookie cookie : cookieList) {
            if (cookie.name().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public List<Cookie> getCookies() {
        return cookieList;
    }

    @Override
    public String getHeader(String name) {
        if (isEmpty(name)
                || !headerMap.containsKey(name)
                || headerMap.get(name).isEmpty())
            return null;
        return headerMap.get(name).get(0);
    }

    @Override
    public List<String> getHeaderNames() {
        return new ArrayList<>(headerMap.keySet());
    }

    @Override
    public List<String> getHeaders(String name) {
        if (isEmpty(name)
                || !headerMap.containsKey(name))
            return new ArrayList<>();
        return headerMap.get(name);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getRequestIp() {
        return requestIp;
    }

    @Override
    public int getRequestPort() {
        return requestPort;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public String toString() {
        return "DefaultNettyHttpRequest [headerMap=" + headerMap
                + ", cookieList=" + cookieList + ", parameterMap=" + parameterMap
                + ", fileMap=" + fileMap + ", uri=" + uri + ", requestIp=" + requestIp
                + ", requestPort=" + requestPort + ", method=" + method + "]";
    }
}
