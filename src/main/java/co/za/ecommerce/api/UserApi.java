package co.za.ecommerce.api;

import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.api.ResetPwdDTOApiResource;
import co.za.ecommerce.dto.api.UserCreateDTOApiResource;
import co.za.ecommerce.dto.api.UserDTOApiResource;
import co.za.ecommerce.dto.user.LoginDTO;
import co.za.ecommerce.dto.user.UpdatePasswordDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class UserApi extends API {

    private final UserService userService;

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello World";
    }

    @PermitAll
    @PostMapping("/register")
    public ResponseEntity<UserCreateDTOApiResource> register(
            @RequestBody
            @Valid
            UserCreateDTO registrationDTO) {
        log.trace("public ResponseEntity<UserCreateDTOApiResource> register(@RequestBody @Valid UserCreateDTO registrationDTO)");
        return ResponseEntity.ok(
                UserCreateDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(mapper
                                .mapObject()
                                .map(userService.createUser(registrationDTO),
                                UserCreateDTO.class)
                        ).message("User registered")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PermitAll
    @PostMapping("/confirm/{phoneNum}/{OTP}")
    public ResponseEntity<UserDTOApiResource> confirmUser(
            @PathVariable @Valid String phoneNum,
            @PathVariable @Valid String OTP) {
        log.trace("public ResponseEntity<UserDTOApiResource> confirmUser(\n" +
                "            @PathVariable String phoneNum,\n" +
                "            @PathVariable String OTP)");
        return ResponseEntity.ok(
                UserDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(userService.activateUserOTP(phoneNum, OTP))
                        .message("User activated")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @PostMapping("/login")
    public ResponseEntity<UserDTOApiResource> login(
            @RequestBody
            @Valid
            LoginDTO loginDTO) {
        log.info("public ResponseEntity<UserDTOApiResource> login(@RequestBody @Valid LoginDTO loginDTO)");
        return ResponseEntity.ok(
                UserDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(userService.loginUser(loginDTO))
                        .message("User logged in")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @PostMapping("/update-password")
    public ResponseEntity<ResetPwdDTOApiResource> updatePassword(
            @RequestBody
            @Valid
            UpdatePasswordDTO updatePasswordDTO) {
        log.info("public ResponseEntity<ResetPwdDTOApiResource> updatePassword(@RequestBody @Valid UpdatePasswordDTO updatePasswordDTO)");
        return ResponseEntity.ok(
                ResetPwdDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(userService.updatePassword(updatePasswordDTO))
                        .message("Password Reset")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

}
