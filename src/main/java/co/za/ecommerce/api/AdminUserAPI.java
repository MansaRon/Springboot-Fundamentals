package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.UsersListApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public interface AdminUserAPI {

    @Operation(tags = "Admin User", summary = "List all users (paginated)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UsersListApiResource.class))),
            @ApiResponse(responseCode = "401", description = "Not authorised",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<UsersListApiResource> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
