package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.checkout.CheckoutDTO;
import co.za.ecommerce.dto.order.AddressDTO;
import co.za.ecommerce.dto.user.UserDTO;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.order.Address;

import java.util.stream.Collectors;

public class CheckoutMapper {

    public static CheckoutDTO toDTO(Checkout checkout) {
        if (checkout == null) return null;

        CheckoutDTO dto = new CheckoutDTO();
        dto.setId(checkout.getId() != null ? checkout.getId().toHexString() : null);
        dto.setCreatedAt(checkout.getCreatedAt());
        dto.setUpdatedAt(checkout.getUpdatedAt());

        dto.setUserDTO(toUserDTO(checkout.getUser()));
        dto.setCartId(checkout.getCart() != null ? checkout.getCart().getId() : null);
        dto.setItems(checkout.getItems() != null
                ? checkout.getItems().stream().map(CartMapper::toCartItemsDTO).collect(Collectors.toList())
                : null);

        dto.setSubtotal(checkout.getSubtotal());
        dto.setDiscount(checkout.getDiscount());
        dto.setTax(checkout.getTax());
        dto.setTotalAmount(checkout.getTotalAmount());
        dto.setPaymentMethod(checkout.getPaymentMethod());
        dto.setShippingAddress(toAddressDTO(checkout.getShippingAddress()));
        dto.setBillingAddress(toAddressDTO(checkout.getBillingAddress()));
        dto.setShippingMethod(checkout.getShippingMethod());
        dto.setEstimatedDeliveryDate(checkout.getEstimatedDeliveryDate() != null
                ? checkout.getEstimatedDeliveryDate().toLocalDate()
                : null);
        dto.setStatus(checkout.getStatus() != null ? checkout.getStatus().name() : null);

        return dto;
    }

    private static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId() != null ? user.getId().toHexString() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
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
}
