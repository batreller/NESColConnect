package nescol.connect.controller;

import nescol.connect.converter.UserConverter;
import nescol.connect.data.GetMeResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "api/user")
public class UserController {
    @Resource
    private UserConverter userConverter;

    @RequestMapping(method = RequestMethod.GET, path="me")
    public GetMeResponse getMe(@AuthenticationPrincipal UsernamePasswordAuthenticationToken user) {
        return userConverter.convert(user);
    }
}
