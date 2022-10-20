package miroshka.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import miroshka.client.model.Message;

import java.io.InputStream;
import java.util.List;


public class JacksonDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            InputStream inputStream = new ByteBufInputStream(byteBuf);
            list.add(mapper.readValue(inputStream, Message.class));
    }

    private final ObjectMapper mapper = new ObjectMapper();
}
