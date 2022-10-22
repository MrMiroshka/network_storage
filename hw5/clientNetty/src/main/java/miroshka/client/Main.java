package miroshka.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import miroshka.client.model.Command;
import miroshka.client.model.Message;
import miroshka.client.network.Client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) {
        //int id = (new Random()).ints(1, (100 + 1)).findFirst().getAsInt();
        //временно передаем id =1, далее у каждого пользователя будет свой id
        String dirUser = whatDirSend(1);
        File file = whatFileSend();

        //C:\java_proj\network_storage\hw5\clientNetty\dir\file.txt

        put(file,dirUser);


    }

    private static void put(File file,String dirUser){
        new Thread(()->{
            Path send = Path.of(dirUser,file.getName());
            try{
                Message message = Message.builder()
                        .command(Command.PUT)
                        .file(file.getName())
                        .length(Files.size(file.toPath()))
                        .dirClinet(dirUser)
                        .data(Files.readAllBytes(file.toPath()))
                        .build();
                new Client().send(message,resposne->{
                    System.out.printf("File %s %s", resposne.getFile(),resposne.getStatus());
                });
            }catch (IOException e){
                throw  new RuntimeException(e);
            }
        }).start();
    }



    private static String whatDirSend(int id) {
        Scanner sc = new Scanner(System.in);
        File file;
        while (true) {
            System.out.println("Оставить путь по умолчанию? (Y\\N)");
            String answer = sc.next();

            switch (answer){
                case ("N"):
                    while (true) {
                        System.out.println("Введите путь папки клиента:");
                        String filePath = sc.next();
                        file = new File(filePath);
                        if(file.exists() && !file.isDirectory()) {
                            sc.close();
                            return filePath;
                        }
                    }
                case ("Y"):
                    return "user-"+id;
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
}
