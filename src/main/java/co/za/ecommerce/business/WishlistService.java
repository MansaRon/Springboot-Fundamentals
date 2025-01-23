package co.za.ecommerce.business;

import co.za.ecommerce.dto.wishlist.WishlistDTO;

import java.util.List;

public interface WishlistService {
    WishlistDTO add(WishlistDTO wishlistDTO);
    List<WishlistDTO> findAll(String userID);
    String delete(String userID, WishlistDTO wishlistDTO);
}
