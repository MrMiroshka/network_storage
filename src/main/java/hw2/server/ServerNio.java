package hw2.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;

public class ServerNio {
    private static final int PORT = 2222;
    private static String FILE_CD = "C:\\test\\hw\\server";


    public static void main(String args[]) throws IOException {
        Path dir =  Paths.get(FILE_CD);
        if (!Files.exists(dir)){
            try {
                Files.createDirectory(dir);
            } catch(FileAlreadyExistsException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String FILE =FILE_CD+"\\"+"test1.txt";

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            try (SocketChannel socketChannel = serverSocketChannel.accept()) {
                try (FileChannel fileChannel = FileChannel.open(Paths.get(FILE), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    fileChannel.transferFrom(socketChannel, 0, Long.MAX_VALUE);
                }
            }
        }
    }
}
