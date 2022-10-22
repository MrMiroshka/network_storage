package miroshka.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigDownload {
    public static final String HOST;
    public static final int PORT;
    //public static final int ID;

    static{
        try(InputStream input = ConfigDownload.class.getResourceAsStream("/application.properties")){
            Properties properties = new Properties();
            properties.load(input);
            HOST = properties.getProperty("host");
            PORT= Integer.parseInt(properties.getProperty("port"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
