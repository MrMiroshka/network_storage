package miroshka.client.config;

import java.io.*;
import java.util.Date;
import java.util.Properties;

public class ConfigDownload {
    public static final String HOST;
    public static final int PORT;
    public static int ID;

    /**
     * Записыавет id клиента в конфигурационный файл, чтобы в следующий раз клиент уже знал свой id
     * и мог попасть в свою папку на сервере
     *
     * @param id - уникальный идентификатор клиента
     */
    public static void setIdToFile(int id) {

        try  {
            FileInputStream in = new FileInputStream("hw5/clientNetty/target/classes/application.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("hw5/clientNetty/target/classes/application.properties");
            props.setProperty("id", Integer.toString(id));
            props.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try (InputStream input = ConfigDownload.class.getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(input);
            input.close();

            props.setProperty("id", Integer.toString(id));
            FileOutputStream out = new FileOutputStream("/application.properties");
            props.store(out, null);
            out.close();
        } catch (IOException e) {
        throw new RuntimeException(e);

         */
    }
    //}

    static {
        try (InputStream input = ConfigDownload.class.getResourceAsStream("/application.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            HOST = properties.getProperty("host");
            if (properties.containsKey("port")) {
                PORT = Integer.parseInt(properties.getProperty("port"));
            } else {
                //если не задан в свойствах то порт по умолчанию 2222
                PORT = 2222;
            }
            if (properties.containsKey("id")) {
                ID = Integer.parseInt(properties.getProperty("id"));
            } else {
                //если нет id в свойствах, то id = 0
                ID = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
