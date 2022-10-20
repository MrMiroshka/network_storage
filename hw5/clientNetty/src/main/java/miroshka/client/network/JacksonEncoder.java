package miroshka.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import miroshka.client.model.Message;

public class JacksonEncoder extends MessageToByteEncoder<Message> {

    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(mapper.writeValueAsBytes(message));
        System.out.println("MSG = " + new String(mapper.writeValueAsBytes(message)));
    }
}
