package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.UserAPI;
import co.za.ecommerce.dto.api.ResetPwdDTOApiResource;
import co.za.ecommerce.dto.api.TokenRefreshDTOApiResource;
import co.za.ecommerce.dto.api.UserCreateDTOApiResource;
import co.za.ecommerce.dto.api.UserDTOApiResource;
import co.za.ecommerce.dto.user.*;
import co.za.ecommerce.utils.DateUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class UserApIImpl extends API implements UserAPI {

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello World";
    }

    @Override
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

    @Override
    @PermitAll
    @PostMapping("/confirm/{phoneNum}/{OTP}")
    public ResponseEntity<UserDTOApiResource> confirmUser(
            @PathVariable @Valid String phoneNum,
            @PathVariable @Valid String OTP) {
        log.trace("public ResponseEntity<UserDTOApiResource> confirmUser(@PathVariable String phoneNum, @PathVariable String OTP)");
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

    @Override
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

    @Override
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

    @Override
    @PermitAll
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutDTO logoutDTO) {
        log.info("public ResponseEntity<?> logout(@RequestBody LogoutDTO logoutDTO)");
        userService.logout(logoutDTO.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshDTOApiResource> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        log.info("public ResponseEntity<TokenRefreshDTOApiResource> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest)");
        return ResponseEntity.ok(
                TokenRefreshDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(refreshTokenService.refreshAccessToken(tokenRefreshRequest.getRefreshToken()))
                        .message("Token refreshed")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/roles/add/{role}")
    public ResponseEntity<UserDTOApiResource> addRole(@PathVariable ObjectId userId, @PathVariable String role) {
        log.info("public ResponseEntity<UserDTOApiResource> addRole(ObjectId userId, String role)");
        return ResponseEntity.ok(
                UserDTOApiResource.builder()
                        .timestamp(DateUtil.instantNow())
                        .data(userService.addRole(userId, role))
                        .message("Role added")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/roles/remove/{role}")
    public ResponseEntity<UserDTOApiResource> removeRole(@PathVariable ObjectId userId, @PathVariable String role) {
        log.info("public ResponseEntity<UserDTOApiResource> removeRole(ObjectId userId, String role)");
        return ResponseEntity.ok(
                UserDTOApiResource.builder()
                        .timestamp(DateUtil.instantNow())
                        .data(userService.removeRole(userId, role))
                        .message("Role removed")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

}
