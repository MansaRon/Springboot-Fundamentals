package co.za.ecommerce.business;

import co.za.ecommerce.dto.user.LoginDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.model.User;

public interface UserService {
    User createUser(UserCreateDTO userCreateDTO);
    UserDTO loginUser(LoginDTO loginDTO);
    UserDTO activateUserOTP(String phoneNum, String otp);

}
