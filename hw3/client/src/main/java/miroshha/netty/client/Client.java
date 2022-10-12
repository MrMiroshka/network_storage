package miroshha.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import miroshha.netty.common.Message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {

    private final int PORT;

    private final String HOST;

    public Client(int port, String host) {
        PORT = port;
        HOST = host;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = whatFileSend();
        //C:\test\hw\hw3\client\dir\file.txt
        Message message = new Message("put", file, Files.readAllBytes(file.toPath()));
        new Client(2222, "localhost").send(message, (response) -> {
            System.out.println("response" + response);
        });
    }

    private static File whatFileSend() {
        Scanner sc = new Scanner(System.in);
        File file;
        while (true) {

            System.out.println("Введите путь до файла, который хотите передать:");
            String filePath = sc.next();
            file = new File(filePath);
            if(file.exists() && !file.isDirectory()) {
                break;
            }
        }
        sc.close();
        return file;
    }

    private void send(Message message, Consumer<String> callback) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(
                            new ObjectEncoder(),
                            new LineBasedFrameDecoder(80),
                            new StringDecoder(StandardCharsets.UTF_8),
                            new ClientHandler(message, callback)
                    );
                }
            });
            ChannelFuture future = client.connect(HOST, PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
