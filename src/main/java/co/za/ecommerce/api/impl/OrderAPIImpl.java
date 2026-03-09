package co.za.ecommerce.api.impl;

import co.za.ecommerce.dto.api.OrderDTOApiResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/order")
public class OrderAPIImpl extends API {

    /**
     * Get order by payment request ID
     *
     * Endpoint: GET /api/v1/payments/order/{paymentRequestId}
     * Called when: Frontend needs order details after payment confirmation
     * Purpose: Retrieve completed order for confirmation page
     *
     * @param paymentRequestId The payment reference ID
     * @return OrderDTO with complete order details
     */
    @GetMapping("/{paymentRequestId}")
    public ResponseEntity<OrderDTOApiResource> getOrderByPaymentRequest(
            @PathVariable String paymentRequestId) {

        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(now())
                        .data(orderService.getOrderByPaymentRequestId(paymentRequestId))
                        .message("Order retrieved successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
