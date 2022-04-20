package cn.szz.netty.http.server.handler;

import java.util.List;

import cn.szz.netty.http.server.entity.NettyHttpFilter;
import cn.szz.netty.http.server.request.NettyHttpRequest;
import cn.szz.netty.http.server.response.NettyHttpResponse;

public class NettyHttpFilterHandler {

    private NettyHttpRequest request;
    private NettyHttpResponse response;

    public NettyHttpFilterHandler(NettyHttpRequest request, NettyHttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public void handlePre(List<NettyHttpFilter> filterList) {
        for (int i = 0; i < filterList.size(); i++) {
            if (!handlePre(filterList.get(i))) {
                return;
            }
        }
    }

    private boolean handlePre(NettyHttpFilter nettyHttpFilter) {
        return nettyHttpFilter.pre(request, response);
    }

    public void handleAfter(List<NettyHttpFilter> filterList) {
        for (int i = filterList.size() - 1; i >= 0; i--) {
            if (!handleAfter(filterList.get(i))) {
                return;
            }
        }
    }

    private boolean handleAfter(NettyHttpFilter nettyHttpFilter) {
        return nettyHttpFilter.after(request, response);
    }
}
