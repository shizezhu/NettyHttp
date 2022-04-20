package cn.szz.netty.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.szz.netty.http.util.CommUtils.*;

import cn.szz.netty.http.server.handler.NettyHttpChannelInboundHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Netty Http 服务器
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:58:43
 */
public final class NettyHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private static NettyHttpServer INSTANCE;

    private String host;
    private int port;
    private int backlog;
    private String dateFormat;

    private NettyHttpServer(String host, int port, int backlog, String dateFormat) {
        this.host = host;
        this.port = port;
        this.backlog = backlog;
        this.dateFormat = dateFormat;
        start();
    }

    public static synchronized NettyHttpServer run(String host, int port, int backlog, String dateFormat) {
        if (INSTANCE == null) {
            INSTANCE = new NettyHttpServer(host, port, backlog, dateFormat);
        }
        return INSTANCE;
    }

    private void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("aggregator", new HttpObjectAggregator(1024 * 1024))
                                    .addLast("handler", new NettyHttpChannelInboundHandler(dateFormat));
                        }
                    }).option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true);
            ChannelFuture channelFuture;
            if (isEmpty(host)) {
                channelFuture = serverBootstrap.bind(port).sync();
            } else {
                channelFuture = serverBootstrap.bind(host, port).sync();
            }
            logger.info("Netty server startup success, serverHost: {}, serverPort: {}, serverBacklog: {}, dateFormat: {}", host, port, backlog, dateFormat);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Netty server startup exception", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
