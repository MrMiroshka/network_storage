package miroshha.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class Server {
    private final int PORT;
    private final static int MAX_OBJECT_SIZE = 1024*1024*1000;
    public Server(int port) {
        PORT = port;
    }

    public static void main(String[] args) throws InterruptedException {
        new Server(2222).run();
    }

    private void run()throws InterruptedException{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup,workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.option(ChannelOption.SO_BACKLOG,128);
            server.childOption(ChannelOption.SO_KEEPALIVE,true);
            server.childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch)throws Exception{
                    ch.pipeline().addLast(
                            new StringEncoder(StandardCharsets.UTF_8),
                            new ObjectDecoder(MAX_OBJECT_SIZE,ClassResolvers.cacheDisabled(null)),
                            new ServerHandler()
                    );
                }
            });
            ChannelFuture future = server.bind(PORT).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
