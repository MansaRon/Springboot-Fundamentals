package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.WishlistService;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.WishlistException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.Wishlist;
import co.za.ecommerce.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    public WishlistDTO add(WishlistDTO wishlistDTO) {
        ObjectId productId = new ObjectId(wishlistDTO.getProductID());
        ObjectId userId = new ObjectId(wishlistDTO.getUserID());

        wishListRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(w -> {
                    throw new WishlistException(
                            HttpStatus.CONFLICT.toString(),
                            "Product is already in your wishlist.",
                            HttpStatus.CONFLICT.value());
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Product not found.",
                        HttpStatus.NOT_FOUND.value()));

        Wishlist wishlist = Wishlist.builder()
                .createdAt(now())
                .updatedAt(now())
                .productId(productId)
                .product(product)
                .userId(userId)
                .build();

        Wishlist saved = wishListRepository.save(wishlist);
        WishlistDTO dto = objectMapper.mapObject().map(saved, WishlistDTO.class);
        dto.setProductDTO(objectMapper.mapObject().map(saved.getProduct(), ProductDTO.class));
        return dto;
    }

    @Override
    public List<WishlistDTO> findAll(String userId) {
        List<Wishlist> items = wishListRepository
                .findAllByUserIdOrderByCreatedAtDesc(new ObjectId(userId));
        return items.stream()
                .map(w -> {
                    WishlistDTO dto = objectMapper.mapObject().map(w, WishlistDTO.class);
                    dto.setProductDTO(objectMapper.mapObject().map(w.getProduct(), ProductDTO.class));
                    return dto;
                })
                .toList();
    }

    @Override
    public String delete(String userId, WishlistDTO wishlistDTO) {
        ObjectId userObjectId = new ObjectId(userId);

        Wishlist wishlist = wishListRepository.findByUserIdAndProductId(
                userObjectId, new ObjectId(wishlistDTO.getProductID())
        ).orElseThrow(() -> new WishlistException(
                HttpStatus.NOT_FOUND.toString(),
                "Wishlist item not found for the given user and product.",
                HttpStatus.NOT_FOUND.value()
        ));

        wishListRepository.delete(wishlist);
        return "Wishlist item deleted";
    }
}
