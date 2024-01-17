package nescol.connect.converter;

import nescol.connect.data.GetMeResponse;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class UserConverter {
    public GetMeResponse convert(Principal user) {
        GetMeResponse target = new GetMeResponse();
        target.setSuccess(user != null);
        assert user != null;
        target.setUserId(user.getName());
        return target;
    }
}
