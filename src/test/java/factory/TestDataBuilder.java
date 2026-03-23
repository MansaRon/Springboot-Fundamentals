package factory;

import co.za.ecommerce.model.Cart;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.checkout.Checkout;
import co.za.ecommerce.model.checkout.CheckoutStatus;
import co.za.ecommerce.model.checkout.DeliverMethod;
import co.za.ecommerce.model.checkout.PaymentMethod;
import co.za.ecommerce.model.order.Address;
import co.za.ecommerce.model.order.Order;
import co.za.ecommerce.model.order.OrderStatus;
import co.za.ecommerce.utils.DateUtil;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {
    public static User buildUser() {
        User user = new User();
        user.setId(new ObjectId());
        user.setName("User");
        user.setEmail("email@example.com");
        user.setPhone("0821234567");
        user.setPassword("SecurePass123@");
        user.setCreatedAt(DateUtil.now());
        user.setUpdatedAt(DateUtil.now());
        return user;
    }

    public static User buildUser(ObjectId id) {
        User user = buildUser();
        user.setId(id);
        return user;
    }

    public static Product buildProduct() {
        return Product.builder()
                .id(new ObjectId())
                .title("Organic Fleece Hoodie")
                .description("Lightweight hoodie made with organic fleece")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .imageUrls(new ArrayList<>(List.of("https://s3.amazonaws.com/hoodie.jpg")))
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Product buildProduct(ObjectId id) {
        return Product.builder()
                .id(id)
                .title("Organic Fleece Hoodie")
                .description("Lightweight hoodie made with organic fleece")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .imageUrls(new ArrayList<>(List.of("https://s3.amazonaws.com/hoodie.jpg")))
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Product buildOutOfStockProduct() {
        return Product.builder()
                .id(new ObjectId())
                .title("Sold Out Hoodie")
                .description("Out of stock product")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(0)
                .imageUrls(new ArrayList<>())
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Product buildLowStockProduct(int quantity) {
        return Product.builder()
                .id(new ObjectId())
                .title("Low Stock Hoodie")
                .description("Limited stock product")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(quantity)
                .imageUrls(new ArrayList<>())
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static CartItems buildCartItem(Product product) {
        return CartItems.builder()
                .product(product)
                .quantity(2)
                .discount(0)
                .tax(0)
                .productPrice(product.getPrice() * 2)
                .build();
    }

    public static CartItems buildCartItem(Product product, int quantity) {
        return CartItems.builder()
                .product(product)
                .quantity(quantity)
                .discount(0)
                .tax(0)
                .productPrice(product.getPrice() * quantity)
                .build();
    }

    public static Cart buildCart(User user, Product product) {
        CartItems item = buildCartItem(product);
        return Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>(List.of(item)))
                .totalPrice(item.getProductPrice())
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Cart buildEmptyCart(User user) {
        return Cart.builder()
                .id(new ObjectId())
                .user(user)
                .cartItems(new ArrayList<>())
                .totalPrice(0.0)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Address buildAddress() {
        return Address.builder()
                .streetAddress("51 Frank Ocean Street")
                .city("Johannesburg")
                .state("Gauteng")
                .country("South Africa")
                .postalCode("2003")
                .build();
    }

    public static Checkout buildPendingCheckout(User user, Cart cart) {
        Checkout checkout = new Checkout();
        checkout.setId(new ObjectId());
        checkout.setUser(user);
        checkout.setCart(cart);
        checkout.setItems(new ArrayList<>(cart.getCartItems()));
        checkout.setStatus(CheckoutStatus.PENDING);
        checkout.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        checkout.setShippingMethod(DeliverMethod.DHL);
        checkout.setShippingAddress(buildAddress());
        checkout.setBillingAddress(buildAddress());
        checkout.setSubtotal(99.98);
        checkout.setDiscount(0);
        checkout.setTax(9.998);
        checkout.setTotalAmount(109.978);
        checkout.setCurrency("ZAR");
        checkout.setCreatedAt(DateUtil.now());
        checkout.setUpdatedAt(DateUtil.now());
        return checkout;
    }

    public static Checkout buildCheckoutWithStatus(User user, Cart cart, CheckoutStatus status) {
        Checkout checkout = buildPendingCheckout(user, cart);
        checkout.setStatus(status);
        return checkout;
    }

    public static Order buildOrder(User user) {
        return Order.builder()
                .id(new ObjectId())
                .orderNumber("ORD-20260314-483920")
                .orderStatus(OrderStatus.CONFIRMED)
                .customerInfo(user)
                .subtotal(99.98)
                .tax(9.998)
                .discount(0)
                .shippingCost(15.99)
                .totalAmount(125.968)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .shippingMethod(DeliverMethod.DHL.name())
                .shippingAddress(buildAddress())
                .billingAddress(buildAddress())
                .orderItems(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    public static Order buildOrderWithStatus(User user, OrderStatus status) {
        Order order = buildOrder(user);
        order.setOrderStatus(status);
        return order;
    }
}
