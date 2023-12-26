package nescol.connect.data;

import lombok.Data;

@Data
public class UserRegisterData {
    private String nescolId;
    private String name;
    private String surname;
    private String password;  // password hashed on frontend, will be hashed on backend again
}
