package miroshka.server.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class User {
    private int id;
    private String dirUser;
}
