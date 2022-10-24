package miroshka.server.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)

public class Message {
 private Command command;
 private  String status;
 private String dirClient;
 private int id;
 private String file;
 private long length;
 private byte[] data;
}
