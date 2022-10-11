package hw2.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;



public class ClientNio {
    private static final int PORT = 2222;
    private static final String HOST = "localhost";
    private static String FILE_CD = "C:\\test\\hw";
    private static String FILE_NAME;
    private static StringBuilder FILE;

    public static void main(String args[]) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Рабочая дирректория по умолчанию - " + FILE_CD);
        Path dir =  Paths.get("C:\\test\\hw");
        if (!Files.exists(dir)){
            try {
                Files.createDirectory(dir);
            } catch(FileAlreadyExistsException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            System.out.println("Введите имя передоваемого файла: ");
            String strFileName = in.nextLine();
            if (!strFileName.isEmpty()) {

                FILE = new StringBuilder(FILE_CD).append('\\').append(strFileName);
                Path fileTransparent =  Paths.get(FILE.toString());

                if (Files.exists(fileTransparent)) {
                    break;
                }else {
                    System.out.print("Такого файла не существует: ");
                }
            } else {
                System.out.print("Файл не может быть пустым: ");
            }
        }


        System.out.print("Начинаем передачу файла - " + FILE);


        InetSocketAddress serverAddress = new InetSocketAddress(HOST, PORT);
        try (SocketChannel socketChannel = SocketChannel.open(serverAddress)) {
            try (FileChannel fileChannel = FileChannel.open(Paths.get(FILE.toString()))) {
                fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            }
        }
    }


}
