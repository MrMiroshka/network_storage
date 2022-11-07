package miroshka.server.logic;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import miroshka.server.config.ConfigDownload;
import miroshka.server.model.Command;
import miroshka.server.model.Message;
import io.netty.channel.Channel;
import miroshka.server.network.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StorageLogic {
    public static void process(Message message, Channel channel) {
        if (message.getCommand().equals(Command.PUT)) {
            String tempPathDir = "hw5\\servernetty\\dir\\".concat(!message.getDirClient().isEmpty()&&message.getDirClient()!=null ? message.getDirClient() : "");
            Path file = Path.of(tempPathDir, message.getFile());
            try {
                Files.createDirectories(Paths.get(tempPathDir));
                Files.createFile(file);
            } catch (FileAlreadyExistsException ignored) {
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
            try (FileOutputStream output = new FileOutputStream(file.toFile())) {
                output.write(message.getData());
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            } finally {
                channel.close();
            }
        }

        if (message.getCommand().equals(Command.GET)) {
            String tempPathDir = "hw5\\servernetty\\dir\\".concat(!message.getDirClient().isEmpty()&&message.getDirClient()!=null ? message.getDirClient() : "");
            String s =findFile(message.getName(),tempPathDir);


            try {
                if (s.isEmpty()){throw new IOException(message.getName() + " - файл не найден");}
                Path file = Path.of(s);
                if (Files.exists(file) && Files.size(file) < ConfigDownload.MAXFILESIZE) {
                    Message messageTemp = Message.builder()
                            .command(message.getCommand())
                            .file(file.getFileName().toString())
                            .status("OK")
                            .length(Files.size(file))
                            .data(Files.readAllBytes(file))
                            .build();
                    channel.writeAndFlush(messageTemp);
                }
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }

        }

        if (message.getCommand().equals(Command.GETUSER)) {

            try {
                int id;
                if (message.getId() == 0) {
                    id = Server.getIdUser();
                } else {
                    id = message.getId();
                }
                Message messageTemp = Message.builder()
                        .command(message.getCommand())
                        .status("OK")
                        .id(id)
                        .build();
                //channel.writeAndFlush(messageTemp);

                ChannelFuture future = channel.writeAndFlush(
                        messageTemp
                );
                future.addListener(ChannelFutureListener.CLOSE);

            } catch (Exception e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }
        }
        if (message.getCommand().equals(Command.DEL)) {
            String tempPathDir = "hw5\\servernetty\\dir\\".concat(!message.getDirClient().isEmpty() ? message.getDirClient() : "");
            Path filePath = Path.of(tempPathDir, message.getName());
            File file = new File(String.valueOf(filePath));
            List<Path> directory = new ArrayList<Path>();
            List<Path> files = new ArrayList<Path>();
            try {
                if (file.isDirectory()) {
                    directory.add(filePath);
                    processFilesFromFolder(file, directory, files);
                    delFiles(files);
                    delDir(directory);
                } else {
                    files.add(filePath);
                    delFiles(files);
                }

                Message messageTemp = Message.builder()
                        .command(message.getCommand())
                        .name(message.getName())
                        .dirClient(message.getDirClient())
                        .status("OK")
                        .build();
                channel.writeAndFlush(messageTemp);

            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }

        }
    }

    private static String findFile(String name,String dirClient) {

        Path filePath = Path.of(dirClient, name);
        File file = new File(dirClient);
        List<Path> directory = new ArrayList<Path>();
        List<Path> files = new ArrayList<Path>();
        try {

                directory.add(Path.of(dirClient));
                processFilesFromFolder(file, directory, files);

        }catch (Exception e){
            e.printStackTrace();
        }
        String pathFile;
        for (Path f:files) {
            if (f.getFileName().toString().equals(name)){
                return f.toString();
            }
        }
        return "";
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
        for (int i = listDirectories.size() - 1; i >= 0; i--) {

            //}
            //for (Path d : listDirectories) {
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
}
