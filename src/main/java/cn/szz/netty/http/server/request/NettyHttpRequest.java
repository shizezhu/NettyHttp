package cn.szz.netty.http.server.request;

import java.util.List;
import java.util.Map;

import cn.szz.netty.http.server.entity.NettyHttpFile;

import io.netty.handler.codec.http.cookie.Cookie;

public interface NettyHttpRequest {

    /**
     * 获取请求IP
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:45:08
     * @return
     */
    String getRequestIp();

    /**
     * 获取请求端口
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:45:15
     * @return
     */
    int getRequestPort();

    /**
     * 获取字节数据
     *
     * @author Shi Zezhu
     * @date 2019年8月7日 下午2:51:15
     * @return
     */
    byte[] getByteData();

    /**
     * 获取请求参数
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:45:28
     * @param name
     * @return
     */
    String getParameter(String name);

    /**
     * 获取请求文件
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:45:38
     * @param name
     * @return
     */
    NettyHttpFile getFile(String name);

    /**
     * 获取所有请求参数
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:45:49
     * @return
     */
    Map<String, List<String>> getParameterMap();

    /**
     * 获取所有请求文件
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:46:03
     * @return
     */
    Map<String, List<NettyHttpFile>> getFileMap();

    /**
     * 获取所有请求参数名称
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:46:11
     * @return
     */
    List<String> getParameterNames();

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
     * 获取请求方法
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:48:07
     * @return
     */
    String getMethod();

    /**
     * 获取请求URI
     *
     * @author Shi Zezhu
     * @date 2019年8月3日 下午1:48:24
     * @return
     */
    String getRequestURI();
}
