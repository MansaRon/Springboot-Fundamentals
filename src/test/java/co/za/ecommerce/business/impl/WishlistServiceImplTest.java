package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import co.za.ecommerce.exception.WishlistException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.Wishlist;
import co.za.ecommerce.repository.WishListRepository;
import co.za.ecommerce.utils.DateUtil;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistServiceImpl Tests")
class WishlistServiceImplTest {

    @Mock private WishListRepository wishListRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private WishlistServiceImpl wishlistService;

    private static final String USER_ID    = "507f1f77bcf86cd799439011";
    private static final String PRODUCT_ID = "507f1f77bcf86cd799439022";

    private ObjectId userObjectId;
    private ObjectId productObjectId;
    private WishlistDTO wishlistDTO;
    private Wishlist savedWishlist;
    private Product product;

    @BeforeEach
    void setUp() {
        userObjectId    = new ObjectId(USER_ID);
        productObjectId = new ObjectId(PRODUCT_ID);

        product = TestDataBuilder.buildProduct(productObjectId);

        ProductDTO productDTO = ProductDTO.builder()
                .id(PRODUCT_ID)
                .title("Organic Fleece Hoodie")
                .description("Lightweight hoodie")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .build();

        wishlistDTO = WishlistDTO.builder()
                .userID(userObjectId)
                .productID(productObjectId)
                .productDTO(productDTO)
                .build();

        savedWishlist = Wishlist.builder()
                .id(new ObjectId())
                .userId(userObjectId)
                .productId(productObjectId)
                .product(product)
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .build();
    }

    @Nested
    @DisplayName("add")
    class Add {
        @Test
        @DisplayName("shouldBuildSaveAndReturnWishlistDTOWhenInputIsValid")
        void shouldBuildSaveAndReturnWishlistDTOWhenInputIsValid() {
            // Arrange
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
            when(wishListRepository.save(any(Wishlist.class))).thenReturn(savedWishlist);
            when(modelMapper.map(any(Wishlist.class), eq(WishlistDTO.class))).thenReturn(wishlistDTO);

            // Act
            WishlistDTO result = wishlistService.add(wishlistDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserID()).isEqualTo(userObjectId);
            assertThat(result.getProductID()).isEqualTo(productObjectId);

            // Verify
            ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
            verify(wishListRepository).save(captor.capture());
            Wishlist captured = captor.getValue();
            assertThat(captured.getUserId()).isEqualTo(userObjectId);
            assertThat(captured.getProductId()).isEqualTo(productObjectId);
            assertThat(captured.getProduct()).isEqualTo(product);
            assertThat(captured.getCreatedAt()).isNotNull();
            assertThat(captured.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("shouldMapProductDTOToProductEntityBeforeSaving")
        void shouldMapProductDTOToProductEntityBeforeSaving() {
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
            when(wishListRepository.save(any(Wishlist.class))).thenReturn(savedWishlist);
            when(modelMapper.map(any(Wishlist.class), eq(WishlistDTO.class))).thenReturn(wishlistDTO);

            // Act
            wishlistService.add(wishlistDTO);

            // Assert
            verify(modelMapper).map(any(ProductDTO.class), eq(Product.class));
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        @DisplayName("shouldReturnMappedWishlistDTOsWhenItemsExist")
        void shouldReturnMappedWishlistDTOsWhenItemsExist() {
            // Arrange
            when(wishListRepository.findAllByUserIdOrderByCreatedAtDesc(eq(userObjectId))).thenReturn(List.of(savedWishlist));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Wishlist.class), eq(WishlistDTO.class)))
                    .thenReturn(wishlistDTO);

            // Act
            List<WishlistDTO> results = wishlistService.findAll(USER_ID);

            // Assert
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUserID()).isEqualTo(userObjectId);
        }

        @Test
        @DisplayName("shouldPassCorrectUserObjectIdToRepository")
        void shouldPassCorrectUserObjectIdToRepository() {
            // Arrange
            when(wishListRepository.findAllByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

            // Act
            wishlistService.findAll(USER_ID);

            // Assert
            verify(wishListRepository).findAllByUserIdOrderByCreatedAtDesc(eq(new ObjectId(USER_ID)));
        }

        @Test
        @DisplayName("shouldReturnEmptyListWhenUserHasNoWishlistItems")
        void shouldReturnEmptyListWhenUserHasNoWishlistItems() {
            // Arrange
            when(wishListRepository.findAllByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

            // Act
            List<WishlistDTO> results = wishlistService.findAll(USER_ID);

            // Assert
            assertThat(results).isEmpty();
            verify(objectMapper, never()).mapObject();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("shouldDeleteWishlistItemAndReturnMessageWhenItemExists")
        void shouldDeleteWishlistItemAndReturnMessageWhenItemExists() {
            // Arrange
            when(wishListRepository.findByUserIdAndProductId(
                    eq(userObjectId), eq(productObjectId)))
                    .thenReturn(Optional.of(savedWishlist));

            // Act
            String result = wishlistService.delete(USER_ID, wishlistDTO);

            // Assert
            assertThat(result).isEqualTo("Wishlist item deleted");
            verify(wishListRepository).delete(savedWishlist);
        }

        @Test
        @DisplayName("shouldThrowIllegalArgumentExceptionWhenUserIdIsNull")
        void shouldThrowIllegalArgumentExceptionWhenUserIdIsNull() {
            assertThatThrownBy(() -> wishlistService.delete(null, wishlistDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid user ID format");

            verify(wishListRepository, never()).delete(any());
        }

        @Test
        @DisplayName("shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalidFormat")
        void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalidFormat() {
            assertThatThrownBy(() -> wishlistService.delete("invalid-id", wishlistDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid user ID format");

            verify(wishListRepository, never()).delete(any());
        }

        @Test
        @DisplayName("shouldThrowWishlistExceptionWhenItemNotFoundForUser")
        void shouldThrowWishlistExceptionWhenItemNotFoundForUser() {
            // Arrange
            when(wishListRepository.findByUserIdAndProductId(any(), any()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> wishlistService.delete(USER_ID, wishlistDTO))
                    .isInstanceOf(WishlistException.class)
                    .hasMessageContaining("Wishlist item not found");

            verify(wishListRepository, never()).delete(any());
        }

        @Test
        @DisplayName("shouldQueryRepositoryWithCorrectUserIdAndProductId")
        void shouldQueryRepositoryWithCorrectUserIdAndProductId() {
            // Arrange
            when(wishListRepository.findByUserIdAndProductId(
                    eq(userObjectId), eq(productObjectId)))
                    .thenReturn(Optional.of(savedWishlist));

            // Act
            wishlistService.delete(USER_ID, wishlistDTO);

            // Assert
            verify(wishListRepository).findByUserIdAndProductId(
                    eq(userObjectId), eq(productObjectId));
        }
    }
}