package nescol.connect.converter;

import nescol.connect.data.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {
    public AuthResponse convert(String token) {
        AuthResponse target = new AuthResponse();
        target.setSuccess(token != null);
        target.setToken(token);
        return target;
    }
}
