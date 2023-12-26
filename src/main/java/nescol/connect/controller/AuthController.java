package nescol.connect.controller;

import nescol.connect.converter.AuthConverter;
import nescol.connect.data.AuthResponse;
import nescol.connect.data.UserLoginData;
import nescol.connect.data.UserRegisterData;
import nescol.connect.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "api/auth")
public class AuthController {
    @Resource
    private UserService userService;

    @Resource
    private AuthConverter authConverter;

    @RequestMapping(method = RequestMethod.POST, path = "login")
    public AuthResponse login(@RequestBody UserLoginData userLoginData) {
        return authConverter.convert(userService.userLogin(userLoginData));
    }

    @RequestMapping(method = RequestMethod.POST, path = "register")
    public AuthResponse register(@RequestBody UserRegisterData userRegisterData) {
        return authConverter.convert(userService.validateAndRegister(userRegisterData));
    }
}
