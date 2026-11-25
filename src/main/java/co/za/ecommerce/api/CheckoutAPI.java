package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.ApiResource;
import co.za.ecommerce.dto.api.CheckoutDTOApiResource;
import co.za.ecommerce.dto.api.OrderDTOApiResource;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
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

/**
 * @author Thendo
 * @date 2025/11/22
 */
public interface CheckoutAPI {

    @Operation(tags = "Checkout", summary = "Create checkout from Cart")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Checkout created successfully",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> initiateCheckout(@PathVariable ObjectId userId);

    @Operation(tags = "Checkout", summary = "Get checkout from cart by Cart ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retrieved checkout by cart id",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> getCheckoutByCart(@PathVariable ObjectId cartId);

    @Operation(tags = "Checkout", summary = "Get checkout from cart by status")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retrieved checkout by cart status",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> getCheckoutsByStatus(@PathVariable String status);

    @Operation(tags = "Checkout", summary = "Update checkout status")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> updateCheckout(@PathVariable ObjectId userId, @Valid @RequestBody CheckoutDTO checkoutDTO);

    @Operation(tags = "Checkout", summary = "Apply a coupon code to the checkout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon applied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckoutDTOApiResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid/expired/exhausted coupon or min spend not met",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Checkout not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<CheckoutDTOApiResource> applyCoupon(@PathVariable ObjectId checkoutId,
                                                       @Valid @RequestBody co.za.ecommerce.dto.coupon.ApplyCouponRequest request);

    @Operation(tags = "Checkout", summary = "Remove the applied coupon from the checkout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon removed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckoutDTOApiResource.class))),
            @ApiResponse(responseCode = "404", description = "Checkout not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<CheckoutDTOApiResource> removeCoupon(@PathVariable ObjectId checkoutId);

    @Operation(tags = "Checkout", summary = "Validate (preview) a coupon code against the checkout without applying it")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result (valid flag + would-be discount)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = co.za.ecommerce.dto.api.CouponValidationApiResource.class))),
            @ApiResponse(responseCode = "404", description = "Checkout not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalApiErrorResponse.class)))
    })
    ResponseEntity<co.za.ecommerce.dto.api.CouponValidationApiResource> validateCoupon(
            @PathVariable ObjectId checkoutId,
            @Valid @RequestBody co.za.ecommerce.dto.coupon.ApplyCouponRequest request);

    @Operation(tags = "Checkout", summary = "Order placed successfully")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = {
                            @Content(schema = @Schema(implementation = OrderDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<OrderDTOApiResource> confirmCheckout(@PathVariable ObjectId checkoutId);

    @Operation(tags = "Checkout", summary = "Active checkout retrieved")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> getUserCheckout(@PathVariable ObjectId userId);

    @Operation(tags = "Checkout", summary = "Cancel checkout")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = {
                            @Content(schema = @Schema(implementation = ApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<ApiResource> cancelCheckout(@PathVariable ObjectId checkoutId);

    @Operation(tags = "Checkout", summary = "Delete user checkout")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = {
                            @Content(schema = @Schema(implementation = CheckoutDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    ResponseEntity<CheckoutDTOApiResource> deleteUserCheckouts(@PathVariable ObjectId userId);
}
