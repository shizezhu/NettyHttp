package cn.szz.netty.http.server.handler;

import static cn.szz.netty.http.util.CommUtils.*;

import cn.szz.netty.http.server.entity.NettyHttpPreflex;

public class NettyHttpException404Handler implements NettyHttpPreflexHandler {

    @Override
    public Object handle(NettyHttpPreflex nettyHttpPreflex) throws Exception {
        if (isNull(nettyHttpPreflex)) return "404, The requested resource is not available";
        return nettyHttpPreflex.invoke(new Object[] {});
    }

}
