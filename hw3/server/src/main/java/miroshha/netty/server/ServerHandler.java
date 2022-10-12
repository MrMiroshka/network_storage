package miroshha.netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miroshha.netty.common.Message;

import java.io.IOException;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        System.out.println("msg = " + message);
        if (message.getCommand().equals("put")) {
            Path root = Path.of("C:\\test\\hw\\hw3\\server\\user-dir");
            Files.createDirectories(root);
            Path file = root.resolve(message.getFile().getPath().replace("client","server"));
            Files.createDirectories(file.getParent());
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException ignored) {
                //ничего не делаем
            } catch (IOException e) {
                e.printStackTrace();
            }
            Files.write(file, message.getData());
        }
        ChannelFuture future = channelHandlerContext.writeAndFlush(String.format("Files %s stored!\n",
                message.getFile().getName()));
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
