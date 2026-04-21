package co.za.ecommerce.business;

import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.model.RefreshToken;
import co.za.ecommerce.model.User;
import org.bson.types.ObjectId;

public interface UserService {
    User createUser(UserCreateDTO userCreateDTO);
    UserDTO loginUser(LoginDTO loginDTO);
    UserDTO activateUserOTP(String phoneNum, String otp);
    ResetPwdDTO updatePassword(UpdatePasswordDTO updatePasswordDTO);
    void logout(String refreshToken);
    UserDTO addRole(ObjectId userId, String role);
    UserDTO removeRole(ObjectId userId, String role);
}
