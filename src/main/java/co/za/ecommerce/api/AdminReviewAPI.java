package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.RatingDTOApiResource;
import co.za.ecommerce.validation.ValidObjectId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface AdminReviewAPI {

    @Operation(tags = "Admin Review", summary = "List all reviews for a product (including hidden)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingDTOApiResource.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<RatingDTOApiResource> getAllReviews(@PathVariable @ValidObjectId String productId);

    @Operation(tags = "Admin Review", summary = "Hide a review from public listings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review hidden",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingDTOApiResource.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<RatingDTOApiResource> hideReview(@PathVariable @ValidObjectId String productId,
                                                    @PathVariable @ValidObjectId String userId);

    @Operation(tags = "Admin Review", summary = "Approve (un-hide) a review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review approved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingDTOApiResource.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<RatingDTOApiResource> approveReview(@PathVariable @ValidObjectId String productId,
                                                       @PathVariable @ValidObjectId String userId);

    @Operation(tags = "Admin Review", summary = "Remove any review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review removed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingDTOApiResource.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<RatingDTOApiResource> removeReview(@PathVariable @ValidObjectId String productId,
                                                      @PathVariable @ValidObjectId String userId);
}
