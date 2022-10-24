package miroshka.client;

import miroshka.client.config.ConfigDownload;
import miroshka.client.model.Command;
import miroshka.client.model.Message;
import miroshka.client.network.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //Узнаем какой id у нас в кофнигурационном файле, если id = 0, то id запросим у сервера
        if (ConfigDownload.ID==0){
            //get();
            getID();
        }

        //int id = (new Random()).ints(1, (100 + 1)).findFirst().getAsInt();
        //временно передаем id =1, далее у каждого пользователя будет свой id
        String dirUser = whatDirSend(ConfigDownload.ID);
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
                        .id(ConfigDownload.ID)
                        .file(file.getName())
                        .length(Files.size(file.toPath()))
                        .dirClient(dirUser)
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

    private static void get(){
        new Thread (()->{
            Message message = Message.builder()
                    .command(Command.GET)
                    .file("file.txt")
                    .build();
            new Client().send(message,resposne->{
                Path file = Path.of("client", resposne.getFile());
                try {
                    Files.createFile(file);
                }catch (FileAlreadyExistsException ignore){

                }catch ( IOException e){
                    throw new RuntimeException(e);
                }
                try(FileOutputStream output = new FileOutputStream(file.toFile())){
                    output.write(resposne.getData());
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }

    private static void getID(){

        Message message = Message.builder()
                .command(Command.GETUSER)
                .id(0)
                .build();
        new Client().send(message,resposne->{

            ConfigDownload.ID = resposne.getId();
            System.out.printf("Id user =  %s ",resposne.getId());
        });

/*
        //new Thread(()->{
        AtomicInteger id = new AtomicInteger();
            try{
                Message message = Message.builder()
                        .command(Command.GETUSER)
                        .build();
                new Client().send(message,resposne->{
                    System.out.printf("File %s %s", resposne.getId(),resposne.getStatus());
                    id.set(resposne.getId());
                });
            }catch (Exception e){
                throw  new RuntimeException(e);
            }
       // }).start();
        return id.get();*/
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
