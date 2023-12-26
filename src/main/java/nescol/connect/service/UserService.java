package nescol.connect.service;

import nescol.connect.data.UserLoginData;
import nescol.connect.data.UserRegisterData;
import nescol.connect.model.User;
import nescol.connect.repository.NescolStudentRepository;
import nescol.connect.repository.UserRepository;
import nescol.connect.security.jwt.JwtTokenProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Resource
    private UserRepository userRepository;

    @Resource
    private NescolStudentRepository nescolStudentRepository;

    @Resource
    private JwtTokenProvider jwtTokenProvider;


    private User createUser(String nescolId, String password) {
        User user = new User();
        String hashedPassword = DigestUtils.sha256Hex(password);
        String secret = DigestUtils.sha256Hex(nescolId + hashedPassword);
        user.setSecret(secret);
        return userRepository.save(user);
    }


    // returns user's token or null
    @Transactional
    public String validateAndRegister(UserRegisterData userRegisterData) {
        int affectedRows = nescolStudentRepository.updateUnusedStudent(userRegisterData.getNescolId(),
                userRegisterData.getName(),
                userRegisterData.getSurname());

        if (affectedRows > 0) {
            User user = createUser(userRegisterData.getNescolId(), userRegisterData.getPassword());  // user registered
            return jwtTokenProvider.createToken(user);
        } else {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userId));
    }

    public String userLogin(UserLoginData userLoginData) {
        User user = userRepository.findBySecret(userLoginData.getSecret());
        if (user != null) {
            return jwtTokenProvider.createToken(user);
        } else {
            return null;
        }
    }
}
