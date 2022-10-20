package miroshka.client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miroshka.client.model.Message;

import java.util.function.Consumer;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private Message message;
    private Consumer<Message> callback;

    public ClientHandler(Message message, Consumer<Message> callback) {
        this.message = message;
        this.callback = callback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        callback.accept(message);
    }
}
