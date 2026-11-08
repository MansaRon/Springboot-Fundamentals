package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.AdminUserAPI;
import co.za.ecommerce.config.AppConstants;
import co.za.ecommerce.dto.api.UsersListApiResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Instant.now;

/**
 * ADMIN only — administrative user management. Non-admin callers get 403.
 */
@Slf4j
@RestController
@RequestMapping("api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserAPIImpl extends API implements AdminUserAPI {

    @Override
    @GetMapping
    public ResponseEntity<UsersListApiResource> getAllUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        log.info("ResponseEntity<UsersListApiResource> getAllUsers(pageNumber, pageSize, sortBy, sortOrder)");
        return ResponseEntity.ok(
                UsersListApiResource.builder()
                        .timestamp(now())
                        .data(userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder))
                        .message("All users retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
