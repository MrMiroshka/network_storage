package miroshka.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import miroshka.client.config.ConfigDownload;
import miroshka.client.model.Message;

import java.util.function.Consumer;

public class Client {
    public void send (Message message, Consumer<Message> callback){
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE,true);
            client.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new JsonObjectDecoder(),
                            new JacksonDecoder(),
                            new JacksonEncoder(),
                            new ClientHandler(message,callback)
                    );
                }
            });
            ChannelFuture future = client.connect(ConfigDownload.HOST,ConfigDownload.PORT).sync();
            future.addListener(f->{
                if (!f.isSuccess()){
                    callback.accept(Message.builder().command(message.getCommand()).status("CONNECTION FAILED").build());
                }
            });
            future.channel().closeFuture().sync();
        }
        catch (InterruptedException e){
            throw  new RuntimeException(e);
        }finally {
            workerGroup.shutdownGracefully();
        }
    }
}
