package nescol.connect.repository;

import nescol.connect.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
    User findBySecret(String secretValue);
}
