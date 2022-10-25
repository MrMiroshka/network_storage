package miroshka.server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import miroshka.server.config.ConfigDownload;
import miroshka.server.model.User;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    public static void main(String[] args) {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        usersList = new ArrayList<>();
        getUpdateUsers();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(mainGroup, workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.option(ChannelOption.SO_BACKLOG, 128);
            server.childOption(ChannelOption.SO_KEEPALIVE, true);
            server.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new JsonObjectDecoder(),
                            new JacksonDecoder(),
                            new JacksonEncoder(),
                            new ServerHandler()
                    );
                }
            });
            ChannelFuture future = server.bind(ConfigDownload.PORT).sync();
            System.out.println("Server running on port " + ConfigDownload.PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static List<User> usersList;

    public static List<User> getUsersList() {
        return usersList;
    }

    public static int getIdUser() {
        getUpdateUsers();
        int id;
        boolean userIdExist = true;
        do {
            id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE - 1);
            for (User us : usersList) {
                if (us.getId() == id) {
                    userIdExist = false;
                    break;
                }
            }
            if (userIdExist == false) {
                userIdExist = true;
            } else {
                break;
            }
        }while (userIdExist);
        //созаем пользрвательскую папку на сервере
        createDir(id);
        return id;
    }

    private static void createDir(int id){
        String tempPathDir = "hw5\\servernetty\\dir\\user-".concat(Integer.toString(id));
        Path file = Path.of(tempPathDir);
        try {
            Files.createDirectories(Paths.get(tempPathDir));
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получаем список пользователей,которые есть уже на сервере.
     */
    public static void getUpdateUsers() {

        try {
            File f = new File("hw5\\servernetty\\dir\\");

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    //отфильруем, чтоб получить наши папки
                    return name.startsWith("user-");
                }
            };

            //получаем массив наших папок
            File[] files = f.listFiles(filter);

            // получаем список пользователей (id + дирректория)
            for (int i = 0; i < files.length; i++) {
                User tempUser = User.builder()
                        .id(Integer.parseInt(files[i].getName().replaceAll("[^0-9]", "")))
                        .dirUser(f.getPath().toString() + files[i].getName())
                        .build();
                if (!usersList.contains(tempUser)) {
                    usersList.add(tempUser);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
