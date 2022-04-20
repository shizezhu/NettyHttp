package cn.szz.netty.http.server.response;

import static cn.szz.netty.http.util.CommUtils.ifIsNullGet;
import static cn.szz.netty.http.util.CommUtils.isEmpty;
import static cn.szz.netty.http.util.CommUtils.isNull;
import static cn.szz.netty.http.util.CommUtils.notEmpty;
import static cn.szz.netty.http.util.CommUtils.notNull;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.AsciiString;

/**
 * 响应对象
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:57:57
 */
public class DefaultNettyHttpResponse implements NettyHttpResponse {

    protected int status;
    private byte[] data;
    private Map<String, List<String>> headerMap = new HashMap<>();
    private List<Cookie> cookieList = new ArrayList<>();

    public DefaultNettyHttpResponse() {
        this(200);
    }

    public DefaultNettyHttpResponse(int status) {
        this.status = status;
    }

    @Override
    public FullHttpResponse getFullHttpResponse() {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(status),
                Unpooled.wrappedBuffer(ifIsNullGet(data, new byte[0])));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=UTF-8");
        fullHttpResponse.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        fullHttpResponse.headers().add(HttpHeaderNames.SERVER, AsciiString.cached("Netty"));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
        fullHttpResponse.headers().add(HttpHeaderNames.DATE, new Date());
        headerMap.forEach(fullHttpResponse.headers()::add);
        fullHttpResponse.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieList));
        return fullHttpResponse;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public DefaultNettyHttpResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public DefaultNettyHttpResponse setData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public DefaultNettyHttpResponse setData(Object data) {
        setData(data, null);
        return this;
    }

    @Override
    public DefaultNettyHttpResponse setData(Object data, String dateFormat) {
        setData(parseData(data, dateFormat));
        return this;
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
    public DefaultNettyHttpResponse setCookie(String name, String value) {
        this.setCookie(new DefaultCookie(name, value));
        return this;
    }

    @Override
    public DefaultNettyHttpResponse setCookie(Cookie cookie) {
        this.cookieList.add(notNull(cookie, "cookie"));
        return this;
    }

    @Override
    public DefaultNettyHttpResponse setCookie(Cookie... cookies) {
        Stream.of(notNull(cookies, "cookies")).forEach(this::setCookie);
        return this;
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
    public DefaultNettyHttpResponse setHeader(String name, String value) {
        notEmpty(notNull(name, "name"), "Empty name");
        notEmpty(notNull(value, "value"), "Empty value");
        if (this.headerMap.containsKey(name)) {
            this.headerMap.get(name).add(value);
        } else {
            this.headerMap.put(name, Stream.of(value).collect(Collectors.toList()));
        }
        return this;
    }

    private byte[] parseData(Object data, String dateFormat) {
        return isNull(data) ? null
                : (data instanceof String ? (String) data
                        : isEmpty(dateFormat) ? JSON.toJSONString(data)
                                : JSON.toJSONStringWithDateFormat(data, dateFormat))
                                        .getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public String toString() {
        return "DefaultNettyHttpResponse [status=" + status + ", headerMap=" + headerMap + ", cookieList=" + cookieList + "]";
    }
}
