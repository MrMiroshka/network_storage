package miroshha.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miroshha.netty.common.Message;

import java.util.function.Consumer;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private  final  Message message;
    private  final Consumer<String> callback;
    public ClientHandler (Message message, Consumer callback){
        this.message = message;
        this.callback = callback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        callback.accept(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
