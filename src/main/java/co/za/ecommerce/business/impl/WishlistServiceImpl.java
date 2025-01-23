package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.WishlistService;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.WishlistException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.Wishlist;
import co.za.ecommerce.repository.WishListRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishListRepository wishListRepository;
    private final ObjectMapper objectMapper;

    @Override
    public WishlistDTO add(WishlistDTO wishlistDTO) {
        Wishlist wishlist = Wishlist.builder()
                .createdAt(now())
                .updatedAt(now())
                .productID(wishlistDTO.getProductID())
                .product(objectMapper.mapObject().map(wishlistDTO.getProductDTO(), Product.class))
                .userID(wishlistDTO.getUserID())
                .build();
        Wishlist saveWishlist = wishListRepository.save(wishlist);
        return objectMapper.mapObject().map(saveWishlist, WishlistDTO.class);
    }

    @Override
    public List<WishlistDTO> findAll(String userId) {
        List<Wishlist> getAllWishListItems = wishListRepository
                .findAllByUserIdOrderByCreatedAtDesc(new ObjectId(userId));
        return getAllWishListItems
                .stream()
                .map(mapWishList -> objectMapper.mapObject().map(mapWishList, WishlistDTO.class))
                .toList();
    }

    @Override
    public String delete(String userId, WishlistDTO wishlistDTO) {
        if (userId == null || !userId.matches("^[a-fA-F0-9]{24}$")) {
            throw new IllegalArgumentException("Invalid user ID format. Must be a 24-character hexadecimal string.");
        }

        ObjectId userObjectId = new ObjectId(userId);

        // Find the wishlist item to delete
        Wishlist wishlist = wishListRepository.findByUserIdAndProductId(
                userObjectId, wishlistDTO.getProductID()
        ).orElseThrow(() -> new WishlistException(
                HttpStatus.NOT_FOUND.toString(),
                "Wishlist item not found for the given user and product.",
                HttpStatus.NOT_FOUND.value()
        ));

        // Delete the wishlist item
        wishListRepository.delete(wishlist);
        return "Wishlist item deleted";
    }
}
