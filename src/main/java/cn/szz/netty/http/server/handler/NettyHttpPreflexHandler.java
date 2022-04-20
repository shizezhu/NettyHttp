package cn.szz.netty.http.server.handler;

import cn.szz.netty.http.server.entity.NettyHttpPreflex;

public interface NettyHttpPreflexHandler {

    Object handle(NettyHttpPreflex nettyHttpPreflex) throws Exception;
}
