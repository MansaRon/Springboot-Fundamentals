package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OrderService;
import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderDTO createOrder(ObjectId userId, Order order) {
        return null;
    }
}
