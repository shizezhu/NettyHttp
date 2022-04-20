package cn.szz.netty.http.server.handler;

import static cn.szz.netty.http.util.CommUtils.notNull;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.szz.netty.http.server.builder.NettyHttpExceptionBuilder;
import cn.szz.netty.http.server.builder.NettyHttpFilterBuilder;
import cn.szz.netty.http.server.builder.NettyHttpUriMapperBuilder;
import cn.szz.netty.http.server.entity.NettyHttpPreflex;
import cn.szz.netty.http.server.request.DefaultNettyHttpRequest;
import cn.szz.netty.http.server.request.NettyHttpRequest;
import cn.szz.netty.http.server.response.DefaultNettyHttpResponse;
import cn.szz.netty.http.server.response.NettyHttpResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Http Handler
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:58:24
 */
public class NettyHttpChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(NettyHttpChannelInboundHandler.class);

    private String dateFormat;

    public NettyHttpChannelInboundHandler(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        NettyHttpRequest request = new DefaultNettyHttpRequest(ctx, msg);
        NettyHttpResponse response = new DefaultNettyHttpResponse();
        execute(request, response);
        ctx.write(response.getFullHttpResponse());
    }

    protected void execute(NettyHttpRequest request, NettyHttpResponse response) throws Exception {
        NettyHttpPreflex nettyHttpPreflex = NettyHttpUriMapperBuilder.getMapping(request.getRequestURI());
        if (notNull(nettyHttpPreflex)) {
            new NettyHttpFilterHandler(request, response).handlePre(NettyHttpFilterBuilder.getFilterList());
            response.setData(new NettyHttpMappingHandler(request, response).handle(nettyHttpPreflex), dateFormat);
            new NettyHttpFilterHandler(request, response).handleAfter(NettyHttpFilterBuilder.getFilterList());
        } else {
            logger.info("Request resource not found: {}", request.getRequestURI());
            response.setStatus(404).setData(new NettyHttpException404Handler().handle(NettyHttpExceptionBuilder.get404()), dateFormat);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyHttpResponse response = new DefaultNettyHttpResponse();
        exception(response, cause);
        ctx.write(response.getFullHttpResponse());
    }

    protected void exception(NettyHttpResponse response, Throwable cause) throws Exception {
        if (cause instanceof InvocationTargetException) {
            Throwable throwable = cause.getCause();
            response.setData(new NettyHttpException500Handler(throwable).handle(NettyHttpExceptionBuilder.get500(throwable.getClass())), dateFormat);
        } else {
            NettyHttpPreflex nettyHttpPreflex = NettyHttpExceptionBuilder.get500(cause.getClass());
            if (notNull(nettyHttpPreflex)) {
                response.setData(new NettyHttpException500Handler(cause).handle(nettyHttpPreflex), dateFormat);
            } else {
                logger.error("Server exception", cause);
                response.setStatus(500).setData(cause.toString(), dateFormat);
            }
        }
    }
}
