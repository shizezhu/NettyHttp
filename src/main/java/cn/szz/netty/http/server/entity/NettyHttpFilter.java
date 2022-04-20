package cn.szz.netty.http.server.entity;

import cn.szz.netty.http.server.request.NettyHttpRequest;
import cn.szz.netty.http.server.response.NettyHttpResponse;

public abstract class NettyHttpFilter {

    public int order() {
        return 0;
    }

    public boolean pre(NettyHttpRequest request, NettyHttpResponse response) {
        return true;
    }

    public boolean after(NettyHttpRequest request, NettyHttpResponse response) {
        return true;
    }
}
