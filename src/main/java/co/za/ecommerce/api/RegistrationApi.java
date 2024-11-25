package co.za.ecommerce.api;

import co.za.ecommerce.business.UserService;
import co.za.ecommerce.dto.api.UserCreateDTOApiResource;
import co.za.ecommerce.dto.user.UserCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class RegistrationApi extends API {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserCreateDTOApiResource> register(
            @RequestBody @Valid UserCreateDTO registrationDTO) {
        log.trace("public ResponseEntity<UserCreateDTOApiResource> register(@RequestBody @Valid UserCreateDTO registrationDTO)");
        return ResponseEntity.ok(
                UserCreateDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(mapper
                                .mapObject()
                                .map(userService.createUser(registrationDTO),
                                UserCreateDTO.class)
                        ).message("User registered")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

}
