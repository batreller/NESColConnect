package nescol.connect.data;

import lombok.Data;

@Data
public class AuthResponse {
    private boolean success;
    private String token;
}
