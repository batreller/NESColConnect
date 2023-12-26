package nescol.connect.data;

import lombok.Data;

@Data
public class UserLoginData {
    private String secret;    // Hash(nescolId + Hash(password))
}
