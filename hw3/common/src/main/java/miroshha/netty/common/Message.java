package miroshha.netty.common;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    private String command;
    private File file;
    private String dir;
    private byte[] data;

    public Message(String command, File file, String dir, byte[] data) {
        this.command = command;
        this.file = file;
        this.dir = dir;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return "Message{" +
                "command='" + command + '\'' +
                ", file=" + file +
                ", dir=" + dir +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
