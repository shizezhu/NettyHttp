package cn.szz.netty.http.server.response;

import java.util.List;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;

public interface NettyHttpResponse {

    /**
     * 获取状态
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:31:08
     * @return
     */
    int getStatus();

    /**
     * 设置状态
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:34:11
     * @param status
     */
    NettyHttpResponse setStatus(int status);

    /**
     * 获取数据
     *
     * @author Shi Zezhu
     * @date 2019年8月6日 下午3:45:43
     * @return
     */
    byte[] getData();

    /**
     * 设置数据
     *
     * @author Shi Zezhu
     * @date 2019年8月6日 下午2:24:43
     * @param data
     * @return
     */
    NettyHttpResponse setData(byte[] data);

    /**
     * 设置数据
     *
     * @author Shi Zezhu
     * @date 2019年8月6日 下午2:24:52
     * @param data
     * @return
     */
    NettyHttpResponse setData(Object data);

    /**
     * 设置数据
     *
     * @author Shi Zezhu
     * @date 2019年8月6日 下午2:25:03
     * @param data
     * @param dateFormat
     * @return
     */
    NettyHttpResponse setData(Object data, String dateFormat);

    /**
     * 获取Cookie值
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:46:28
     * @param name
     * @return
     */
    String getCookieValue(String name);

    /**
     * 获取Cookie
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:46:40
     * @param name
     * @return
     */
    Cookie getCookie(String name);

    /**
     * 获取所有Cookie
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:46:49
     * @return
     */
    List<Cookie> getCookies();

    /**
     * 设置Cookie
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:31:21
     * @param name
     * @param value
     */
    NettyHttpResponse setCookie(String name, String value);

    /**
     * 设置Cookie
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:31:21
     * @param cookie
     */
    NettyHttpResponse setCookie(Cookie cookie);

    /**
     * 设置Cookie
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:31:21
     * @param cookies
     */
    NettyHttpResponse setCookie(Cookie... cookies);

    /**
     * 获取请求头部信息
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:47:01
     * @param name
     * @return
     */
    String getHeader(String name);

    /**
     * 获取请求头部信息名称
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:47:17
     * @return
     */
    List<String> getHeaderNames();

    /**
     * 获取请求头部信息
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:47:52
     * @param name
     * @return
     */
    List<String> getHeaders(String name);

    /**
     * 设置消息头部信息
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午3:31:40
     * @param name
     * @param value
     */
    NettyHttpResponse setHeader(String name, String value);

    /**
     * 获取NettyResponse对象
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午4:03:24
     * @return
     */
    FullHttpResponse getFullHttpResponse();
}
