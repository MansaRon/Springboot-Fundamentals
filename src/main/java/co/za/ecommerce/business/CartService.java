package co.za.ecommerce.business;

import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.model.Cart;
import org.bson.types.ObjectId;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(ObjectId userId, ObjectId productId, int quantity);
    CartDTO getUserCartWithItems(ObjectId userId);
    CartDTO updateProductInCart(ObjectId userId, ObjectId productId, int newQuantity);
    CartDTO deleteProductFromCart(ObjectId userId, ObjectId productId);
}
