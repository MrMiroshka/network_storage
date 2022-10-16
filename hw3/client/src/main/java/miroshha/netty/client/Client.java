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
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {

    private static String dirClientDefault;
    private final int PORT;

    private final String HOST;

    private final int ID;


    public Client(int port, String host,int id) {
        PORT = port;
        HOST = host;
        ID = id;
        dirClientDefault = "C:\\test\\hw\\hw3\\client\\user-"+id;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        int id = (new Random()).ints(1, (100 + 1)).findFirst().getAsInt();
        String dirUser = whatDirSend( id);
        File file = whatFileSend();

        //C:\test\hw\hw3\client\dir\file.txt
        Message message = new Message("put", file, dirUser,Files.readAllBytes(file.toPath()));
        new Client(2222, "localhost",id).send(message, (response) -> {
            System.out.println("response" + response);
        });
    }

    private static String whatDirSend(int id) {
        Scanner sc = new Scanner(System.in);
        File file;
        while (true) {
            System.out.println("Оставить путь по умолчанию? (Y\\N)");
            String answer = sc.next();

            switch (answer){
                case ("Y"):
                    while (true) {
                        System.out.println("Введите путь папки клиента:");
                        String filePath = sc.next();
                        file = new File(filePath);
                        if(file.exists() && !file.isDirectory()) {
                            sc.close();
                            return filePath;
                        }
                    }
                case ("N"):
                    return "C:\\test\\hw\\hw3\\client\\user-"+id;
            }

        }

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
