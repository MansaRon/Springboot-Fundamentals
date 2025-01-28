package co.za.ecommerce.repository;

import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import org.bson.types.ObjectId;

public interface CustomCartItemRepository {
    Product findProductById(ObjectId productId);
    CartItems findCartItemsByProductIdAndCartId(ObjectId cartId, ObjectId productId);
    void deleteCartItemByProductIdAndCartId(ObjectId cartId, ObjectId productId);
}
