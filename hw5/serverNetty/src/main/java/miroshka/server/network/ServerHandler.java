package miroshka.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miroshka.server.model.Message;
import miroshka.server.logic.StorageLogic;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        System.out.println("msg = " + message);
        StorageLogic.process(message,channelHandlerContext.channel());
    }
}
