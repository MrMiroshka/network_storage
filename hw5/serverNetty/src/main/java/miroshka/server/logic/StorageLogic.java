package miroshka.server.logic;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import miroshka.server.config.ConfigDownload;
import miroshka.server.model.Command;
import miroshka.server.model.Message;
import io.netty.channel.Channel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageLogic {
    public static void process(Message message, Channel channel) {
        if (message.getCommand().equals(Command.PUT)) {
            String tempPathDir = "hw5\\servernetty\\dir\\".concat(!message.getDirClinet().isEmpty()?message.getDirClinet():"");
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
            try(FileOutputStream output = new FileOutputStream(file.toFile())){
                output.write(message.getData());
            }catch (IOException e){
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }finally {
                channel.close();
            }
        }

        if (message.getCommand().equals(Command.GET)) {
            Path file = Path.of("server",message.getFile());
            try{
                if(Files.exists(file) && Files.size(file) < ConfigDownload.MAXFILESIZE){
                    Message messageTemp = Message.builder()
                            .command(message.getCommand())
                            .file(file.getFileName().toString())
                            .status("OK")
                            .length(Files.size(file))
                            .data(Files.readAllBytes(file))
                            .build();
                    channel.writeAndFlush(messageTemp);
                }
            }catch (IOException e){
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            }finally {
                channel.close();
            }

        }
    }
}
