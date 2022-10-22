package miroshka.server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import miroshka.server.config.ConfigDownload;

public class Server {
    public static void main(String[] args) {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap server = new ServerBootstrap();
            server.group(mainGroup,workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.option(ChannelOption.SO_BACKLOG,128);
            server.childOption(ChannelOption.SO_KEEPALIVE,true);
            server.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(8*1024,32*1024));
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new JsonObjectDecoder(),
                            new JacksonDecoder(),
                            new JacksonEncoder(),
                            new ServerHandler()
                    );
                }
            });
            ChannelFuture future = server.bind(ConfigDownload.PORT).sync();
            System.out.println("Server running on port " + ConfigDownload.PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw  new RuntimeException(e);
        }finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
