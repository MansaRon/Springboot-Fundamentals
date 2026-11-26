package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.ApiResource;
import co.za.ecommerce.dto.api.CouponApiResource;
import co.za.ecommerce.dto.api.CouponListApiResource;
import co.za.ecommerce.dto.coupon.CouponDTO;
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

public interface AdminCouponAPI {

    @Operation(tags = "Admin Coupon", summary = "Create a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coupon created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Coupon code already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authorisation invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<CouponApiResource> create(@Valid @RequestBody CouponDTO request);

    @Operation(tags = "Admin Coupon", summary = "List all coupons")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupons retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponListApiResource.class)))
    })
    ResponseEntity<CouponListApiResource> getAll();

    @Operation(tags = "Admin Coupon", summary = "Get a coupon by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponApiResource.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<CouponApiResource> getById(@PathVariable ObjectId id);

    @Operation(tags = "Admin Coupon", summary = "Update a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<CouponApiResource> update(@PathVariable ObjectId id, @Valid @RequestBody CouponDTO request);

    @Operation(tags = "Admin Coupon", summary = "Delete a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon deleted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResource.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<ApiResource> delete(@PathVariable ObjectId id);
}
