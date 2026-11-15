package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.ApiResource;
import co.za.ecommerce.dto.api.LookupValueApiResource;
import co.za.ecommerce.dto.api.LookupValueListApiResource;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminLookupAPI {

    @Operation(tags = "Admin Lookup", summary = "List all values for a type (active and inactive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lookup values retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LookupValueListApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid lookup type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<LookupValueListApiResource> getAllByType(@PathVariable String type);

    @Operation(tags = "Admin Lookup", summary = "Create a lookup value")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lookup value created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LookupValueApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authorised",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<LookupValueApiResource> create(@Valid @RequestBody LookupValueDTO request);

    @Operation(tags = "Admin Lookup", summary = "Update a lookup value")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lookup value updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LookupValueApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lookup value not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    // No @Valid: update is partial — only provided fields are applied, so the create-time
    // required-field constraints (type/code/label) must not be enforced here.
    ResponseEntity<LookupValueApiResource> update(@PathVariable ObjectId id, @RequestBody LookupValueDTO request);

    @Operation(tags = "Admin Lookup", summary = "Delete a lookup value")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lookup value deleted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResource.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lookup value not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<ApiResource> delete(@PathVariable ObjectId id);
}
