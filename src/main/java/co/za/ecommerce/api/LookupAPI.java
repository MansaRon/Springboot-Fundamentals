package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.LookupValueListApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface LookupAPI {

    @Operation(tags = "Lookup", summary = "Get active lookup values by type")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lookup values retrieved successfully",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LookupValueListApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid lookup type",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<LookupValueListApiResource> getByType(@PathVariable String type);
}
