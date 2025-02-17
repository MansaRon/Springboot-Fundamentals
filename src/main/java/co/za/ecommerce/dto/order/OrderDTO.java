package co.za.ecommerce.dto.order;

import co.za.ecommerce.dto.base.EntityDTO;
import co.za.ecommerce.model.order.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends EntityDTO {
    private CustomerDTO customerDTO;
    private List<OrderItemsDTO> orderItemsDTOS;
    private PaymentDTO paymentDTO;
    private AddressDTO shippingAddressDTO;
    private Address billingAddressDTO;
    private String orderStatus;
}
