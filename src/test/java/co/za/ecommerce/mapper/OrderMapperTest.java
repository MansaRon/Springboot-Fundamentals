package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.dto.order.OrderItemsDTO;
import co.za.ecommerce.dto.order.PaymentDTO;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.*;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    @Nested
    @DisplayName("mapToOrderDTO")
    class MapToOrderDTO {

        @Test
        @DisplayName("shouldReturnNullWhenOrderIsNull")
        void shouldReturnNullWhenOrderIsNull() {
            assertThat(OrderMapper.mapToOrderDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapFullOrderToDTO")
        void shouldMapFullOrderToDTO() {
            User user = TestDataBuilder.buildUser();
            Order order = TestDataBuilder.buildOrder(user);

            OrderDTO dto = OrderMapper.mapToOrderDTO(order);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(order.getId().toHexString());
            assertThat(dto.getOrderNumber()).isEqualTo("ORD-20260314-483920");
            assertThat(dto.getOrderStatus()).isEqualTo("CONFIRMED");
            assertThat(dto.getSubtotal()).isEqualTo(99.98);
            assertThat(dto.getTotalAmount()).isEqualTo(164.98);
        }

        @Test
        @DisplayName("shouldMapCustomerInfoToDTO")
        void shouldMapCustomerInfoToDTO() {
            User user = TestDataBuilder.buildUser();
            Order order = TestDataBuilder.buildOrder(user);

            OrderDTO dto = OrderMapper.mapToOrderDTO(order);

            assertThat(dto.getCustomerInfo()).isNotNull();
            assertThat(dto.getCustomerInfo().getEmail()).isEqualTo("email@example.com");
            assertThat(dto.getCustomerInfo().getName()).isEqualTo("User");
        }

        @Test
        @DisplayName("shouldMapNullCustomerInfoToNull")
        void shouldMapNullCustomerInfoToNull() {
            User user = TestDataBuilder.buildUser();
            Order order = TestDataBuilder.buildOrder(user);
            order.setCustomerInfo(null);

            OrderDTO dto = OrderMapper.mapToOrderDTO(order);

            assertThat(dto.getCustomerInfo()).isNull();
        }

        @Test
        @DisplayName("shouldMapShippingAndBillingAddresses")
        void shouldMapShippingAndBillingAddresses() {
            User user = TestDataBuilder.buildUser();
            Order order = TestDataBuilder.buildOrder(user);

            OrderDTO dto = OrderMapper.mapToOrderDTO(order);

            assertThat(dto.getShippingAddress()).isNotNull();
            assertThat(dto.getShippingAddress().getCity()).isEqualTo("Johannesburg");
            // Contact fields must survive mapping (previously dropped).
            assertThat(dto.getShippingAddress().getFullName()).isEqualTo("Frank Ocean");
            assertThat(dto.getShippingAddress().getPhoneNumber()).isEqualTo("0821234567");
            assertThat(dto.getShippingAddress().getEmail()).isEqualTo("frank@example.com");
            assertThat(dto.getShippingAddress().getApartment()).isEqualTo("Unit 4B");
            assertThat(dto.getBillingAddress()).isNotNull();
        }

        @Test
        @DisplayName("shouldMapNullAddressesToNull")
        void shouldMapNullAddressesToNull() {
            User user = TestDataBuilder.buildUser();
            Order order = TestDataBuilder.buildOrder(user);
            order.setShippingAddress(null);
            order.setBillingAddress(null);

            OrderDTO dto = OrderMapper.mapToOrderDTO(order);

            assertThat(dto.getShippingAddress()).isNull();
            assertThat(dto.getBillingAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("mapToOrderItemsDTO")
    class MapToOrderItemsDTO {

        @Test
        @DisplayName("shouldReturnNullWhenOrderItemsIsNull")
        void shouldReturnNullWhenOrderItemsIsNull() {
            assertThat(OrderMapper.mapToOrderItemsDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapOrderItemsToDTO")
        void shouldMapOrderItemsToDTO() {
            var product = TestDataBuilder.buildProduct();
            OrderItems orderItem = new OrderItems();
            orderItem.setId(new ObjectId());
            orderItem.setProduct(product);
            orderItem.setQuantity(2);
            orderItem.setUnitPrice(49.99);
            orderItem.setTotalPrice(99.98);
            orderItem.setDiscount(5.0);
            orderItem.setTax(15.0);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());

            OrderItemsDTO dto = OrderMapper.mapToOrderItemsDTO(orderItem);

            assertThat(dto).isNotNull();
            assertThat(dto.getProductTitle()).isEqualTo("Organic Fleece Hoodie");
            assertThat(dto.getQuantity()).isEqualTo(2);
            // Per-item pricing must be mapped (previously always 0).
            assertThat(dto.getUnitPrice()).isEqualTo(49.99);
            assertThat(dto.getTotalPrice()).isEqualTo(99.98);
            assertThat(dto.getDiscount()).isEqualTo(5.0);
            assertThat(dto.getTax()).isEqualTo(15.0);
        }
    }

    @Nested
    @DisplayName("mapToPaymentDetailsDTO")
    class MapToPaymentDetailsDTO {

        @Test
        @DisplayName("shouldReturnNullWhenPaymentDetailsIsNull")
        void shouldReturnNullWhenPaymentDetailsIsNull() {
            assertThat(OrderMapper.mapToPaymentDetailsDTO(null)).isNull();
        }

        @Test
        @DisplayName("shouldMapPaymentDetailsToDTO")
        void shouldMapPaymentDetailsToDTO() {
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setTransactionId("TXN-123");
            paymentDetails.setPaymentStatus("SUCCESS");

            PaymentDTO dto = OrderMapper.mapToPaymentDetailsDTO(paymentDetails);

            assertThat(dto).isNotNull();
            assertThat(dto.getTransactionId()).isEqualTo("TXN-123");
            assertThat(dto.getPaymentStatus()).isEqualTo("SUCCESS");
        }
    }
}