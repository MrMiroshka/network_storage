package miroshka.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        //Узнаем какой id у нас в кофнигурационном файле, если id = 0, то id запросим у сервера
        if (ConfigDownload.ID == 0) {
            //get();
            getID();
            ConfigDownload.setIdToFile(ConfigDownload.ID);
        }



        String dirUser = whatDirSend(ConfigDownload.ID);



        //удаляем файл
        del(whatFileDel(),dirUser);
        //C:\java_proj\network_storage\hw5\clientNetty\dir\file.txt

        //отправить файл на сервер
        //File file = whatFileSend();
        //put(file, dirUser);


    }

    private static void put(File file, String dirUser) {
        new Thread(() -> {
            Path send = Path.of(dirUser, file.getName());
            try {
                Message message = Message.builder()
                        .command(Command.PUT)
                        .id(ConfigDownload.ID)
                        .file(file.getName())
                        .length(Files.size(file.toPath()))
                        .dirClient(dirUser)
                        .data(Files.readAllBytes(file.toPath()))
                        .build();
                new Client().send(message, response -> {
                    System.out.printf("File %s %s", response.getFile(), response.getStatus());
                    System.out.println();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void get() {
        new Thread(() -> {
            Message message = Message.builder()
                    .command(Command.GET)
                    .file("file.txt")
                    .build();
            new Client().send(message, response -> {
                Path file = Path.of("client", response.getFile());
                try {
                    Files.createFile(file);
                } catch (FileAlreadyExistsException ignore) {

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try (FileOutputStream output = new FileOutputStream(file.toFile())) {
                    output.write(response.getData());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }

    private static void del(File file, String dirUser) {

        Message message = Message.builder()
                .command(Command.DEL)
                .id(ConfigDownload.ID)
                .name(file.getName())
                .dirClient(dirUser)
                .build();
        new Client().send(message, response -> {
            if (response.getStatus().equals("FILE ERROR")) {
                System.out.printf("No Del file on server =  %s, dir = %s ", response.getName(), response.getDirClient());
            }else{
                System.out.printf("Del file on server=  %s, dir = %s ", response.getName(), response.getDirClient());
                System.out.println();
                delFiles(file);
            }
        });
    }


    private static void delFiles(File file){
        List<Path> directory = new ArrayList<Path>();
        List<Path> files = new ArrayList<Path>();
        try {
            if (file.isDirectory()) {
                directory.add(file.toPath());
                processFilesFromFolder(file, directory, files);
                delFiles(files);
                delDir(directory);
            } else {
                files.add(file.toPath());
                delFiles(files);
            }
        } catch (IOException e) {
          e.printStackTrace();
        }
    }


    public static void processFilesFromFolder(File folder, List<Path> directory, List<Path> files) throws IOException {
        File[] folderEntries = folder.listFiles();
        for (File entry : folderEntries) {
            if (entry.isDirectory()) {
                directory.add(entry.toPath());
                processFilesFromFolder(entry, directory, files);
                continue;
            }
            files.addAll(getListFilesPath(entry.toPath()));
        }
    }

    public static List<Path> getListFilesPath(Path file) throws IOException {
        return Files.walk(file)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    public static void delFiles(List<Path> listPathFiles) throws IOException {
        for (Path p : listPathFiles) {
            File file = new File(p.toString());
            if (file.delete()) {
                System.out.println(p + "файл удален");
            } else {
                System.out.println(p + " не обнаружено");
                throw new IOException(p + " нет такого файла на сервере");
            }
        }
    }

    public static void delDir(List<Path> listDirectories) throws IOException {
        for (int i = listDirectories.size()-1; i >= 0 ; i--) {
            File fileDir = new File((listDirectories.get(i)).toString());
            if (fileDir.isDirectory()) {
                if (fileDir.delete()) {
                    System.out.println(fileDir + " папка была удалена");
                } else {
                    System.out.println(fileDir + " папка не была удаленаа");
                    throw new IOException(fileDir + " папка не пуста");
                }
            }
        }
    }
    private static void getID() {

        Message message = Message.builder()
                .command(Command.GETUSER)
                .id(0)
                .build();
        new Client().send(message, response -> {
            ConfigDownload.ID = response.getId();
            System.out.printf("Id user =  %s ", response.getId());
            System.out.println();
        });

    }


    private static String whatDirSend(int id) {
        Scanner sc = new Scanner(System.in);
        File file;
        while (true) {
            System.out.println("Оставить путь по умолчанию? (Y\\N)");
            String answer = sc.next();

            switch (answer) {
                case ("N"):
                    while (true) {
                        System.out.println("Введите путь папки клиента:");
                        String filePath = sc.next();
                        file = new File(filePath);
                        if (file.exists() && !file.isDirectory()) {
                            sc.close();
                            return filePath;
                        }
                    }
                case ("Y"):
                    return "user-" + id;
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
            if (file.exists() && !file.isDirectory()) {
                break;
            }
        }
        sc.close();
        return file;
    }


    private static File whatFileDel() {
        Scanner sc = new Scanner(System.in);
        File file;
        while (true) {

            System.out.println("Введите путь до файла или директории, который/ую хотите удалить:");
            String filePath = sc.next();
            file = new File(filePath);
            if (file.exists()) {
                break;
            }
        }
        sc.close();
        return file;
    }
}
