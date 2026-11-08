package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.ResetPwdDTOApiResource;
import co.za.ecommerce.dto.api.UsersListApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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

    @Operation(tags = "Admin User", summary = "Admin-triggered password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset; temporary password emailed to the user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResetPwdDTOApiResource.class))),
            @ApiResponse(responseCode = "400", description = "User not active",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authorised",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<ResetPwdDTOApiResource> resetPassword(@PathVariable ObjectId id);
}
