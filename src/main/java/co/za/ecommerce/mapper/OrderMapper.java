package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.order.*;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.model.order.PaymentDetails;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO mapToOrderDTO(Order order) {
        if (order == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId() != null ? order.getId().toHexString() : null);
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCustomerDTO(toUserDTO(order.getCustomerInfo()));
        dto.setOrderItemsDTOS(order.getOrderItems().stream().map(OrderMapper::mapToOrderItemsDTO).collect(Collectors.toList()));
        dto.setPaymentDTO(mapToPaymentDetailsDTO(order.getPaymentDetails()));
        dto.setShippingAddressDTO(toAddressDTO(order.getShippingAddress()));
        dto.setBillingAddressDTO(toAddressDTO(order.getBillingAddress()));
        dto.setOrderStatus(order.getOrderStatus().toString());
        dto.setShippingMethod(order.getShippingMethod());
        dto.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscount(order.getDiscount());
        dto.setTax(order.getTax());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTransactionId(order.getTransactionId());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setShippingCost(order.getShippingCost());

        return dto;
    }

    private static CustomerDTO toUserDTO(User user) {
        if (user == null) return null;
        CustomerDTO dto = new CustomerDTO();
        dto.setId(user.getId() != null ? user.getId().toHexString() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setFirstName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setNumber(user.getPhone());
        return dto;
    }

    private static AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setStreetAddress(address.getStreetAddress());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());
        return dto;
    }

    public static OrderItemsDTO mapToOrderItemsDTO(OrderItems orderItems) {
        if (orderItems == null) return null;
        OrderItemsDTO dto = new OrderItemsDTO();
        dto.setId(orderItems.getId() != null ? orderItems.getId().toHexString() : null);
        dto.setQuantity(orderItems.getQuantity());
        dto.setTotalPrice(orderItems.getTotalPrice());
        dto.setImageUrl(orderItems.getImageUrl());
        dto.setProductId(orderItems.getId().toHexString());

        return dto;
    }

    public static PaymentDTO mapToPaymentDetailsDTO(PaymentDetails paymentDetails) {
        if (paymentDetails == null) return null;
        return null;
    }
}
