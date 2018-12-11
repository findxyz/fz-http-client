package xyz.fz.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xyz.fz.http.client.ssl.SSLEngineFactory;

import java.net.InetSocketAddress;

@Component
public class HttpClient {

    @Value("${http.server.host}")
    private String httpServerHost;

    @Value("${http.server.port}")
    private int httpServerPort;

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(httpServerHost, httpServerPort))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addFirst(new SslHandler(SSLEngineFactory.create()));
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpContentDecompressor());
                            ch.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                    "/hello", Unpooled.copiedBuffer("name=fz", CharsetUtil.UTF_8));
            request.headers()
                    .add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Accept-Encoding", "gzip, deflate, br")
                    .add("Content-Length", request.content().readableBytes());
            ctx.writeAndFlush(request);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
            System.out.println("============================ full response header ============================");
            System.out.println(msg);
            System.out.println("============================ full response body ============================");
            System.out.println(msg.content().toString(CharsetUtil.UTF_8).replace("<br/>", "\r\n"));
            ctx.channel().close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
