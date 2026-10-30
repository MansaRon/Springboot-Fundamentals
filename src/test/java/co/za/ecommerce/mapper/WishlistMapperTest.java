package co.za.ecommerce.mapper;

import co.za.ecommerce.business.impl.WishlistServiceImpl;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.Wishlist;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.repository.WishListRepository;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the real {@link ObjectMapper} (not a mock) through the service, guarding the
 * Wishlist -> WishlistDTO mapping. WishlistDTO.productID is ambiguous to ModelMapper
 * (Wishlist.productId vs the embedded product's id), so the service builds the DTO
 * explicitly — this test fails if that mapping regresses.
 */
@DisplayName("Wishlist -> WishlistDTO mapping (real ObjectMapper)")
class WishlistMapperTest {

    private final WishListRepository wishListRepository = mock(WishListRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final WishlistServiceImpl service =
            new WishlistServiceImpl(wishListRepository, productRepository, new ObjectMapper());

    @Test
    @DisplayName("findAll sources productID from Wishlist.productId, not the embedded product's id")
    void findAllMapsWishlistWithoutAmbiguity() {
        ObjectId userId = new ObjectId();
        ObjectId productId = new ObjectId();
        // Embedded product carries its OWN id — distinct from productId, which is the
        // crux of the ambiguity the mapping must resolve in favour of productId.
        Product product = TestDataBuilder.buildProduct(new ObjectId());

        Wishlist wishlist = Wishlist.builder()
                .id(new ObjectId())
                .userId(userId)
                .productId(productId)
                .product(product)
                .build();

        when(wishListRepository.findAllByUserIdOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(wishlist));

        List<WishlistDTO> result = service.findAll(userId.toString());

        assertThat(result).hasSize(1);
        WishlistDTO dto = result.get(0);
        assertThat(dto.getProductID()).isEqualTo(productId.toString());
        assertThat(dto.getProductID()).isNotEqualTo(product.getId().toString());
        assertThat(dto.getUserID()).isEqualTo(userId.toString());
        assertThat(dto.getProductDTO()).isNotNull();
    }
}
