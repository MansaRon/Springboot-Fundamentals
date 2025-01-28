package co.za.ecommerce.business;

import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.model.Cart;
import org.bson.types.ObjectId;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(ObjectId cartId, ObjectId productId, int quantity);
    List<Cart> getAllCarts();
    CartDTO getCart(ObjectId userId, ObjectId cartId);
    CartDTO updateProductInCart(ObjectId cartId, ObjectId productId);
    CartDTO updateProductQuantityInCart(ObjectId cartId, ObjectId productId, Integer quantity);
    void deleteProductFromCart(ObjectId cartId, ObjectId productId);
}
