package co.za.ecommerce.business.impl;

import co.za.ecommerce.exception.InventoryException;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.repository.ProductRepository;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Tests")
class InventoryServiceImplTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private InventoryServiceImpl inventoryService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = TestDataBuilder.buildProduct();
        product.setQuantity(10);
    }

    @Nested
    @DisplayName("ReduceInventory")
    class ReduceInventory {

        @Test
        @DisplayName("shouldReduceInventorySuccessfully")
        void shouldReduceInventorySuccessfully() {
            CartItems item = TestDataBuilder.buildCartItem(product, 3);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            inventoryService.reduceInventory(List.of(item));

            assertThat(product.getQuantity()).isEqualTo(7);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("shouldTriggerLowStockWarnWhenQuantityDropsToFiveOrBelow")
        void shouldTriggerLowStockWarnWhenQuantityDropsToFiveOrBelow() {
            product.setQuantity(6);
            CartItems item = TestDataBuilder.buildCartItem(product, 3);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThatCode(() -> inventoryService.reduceInventory(List.of(item)))
                    .doesNotThrowAnyException();

            assertThat(product.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("shouldThrowWhenProductNotFoundDuringReduction")
        void shouldThrowWhenProductNotFoundDuringReduction() {
            CartItems item = TestDataBuilder.buildCartItem(product, 1);
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.reduceInventory(List.of(item)))
                    .isInstanceOf(InventoryException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("shouldThrowWhenRequestedQuantityExceedsStock")
        void shouldThrowWhenRequestedQuantityExceedsStock() {
            product.setQuantity(2);
            CartItems item = TestDataBuilder.buildCartItem(product, 5);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> inventoryService.reduceInventory(List.of(item)))
                    .isInstanceOf(InventoryException.class)
                    .hasMessageContaining("Insufficient inventory");
        }

        @Test
        @DisplayName("shouldReduceInventoryForMultipleItems")
        void shouldReduceInventoryForMultipleItems() {
            Product product2 = TestDataBuilder.buildProduct(new ObjectId());
            product2.setQuantity(5);

            CartItems item1 = TestDataBuilder.buildCartItem(product, 2);
            CartItems item2 = TestDataBuilder.buildCartItem(product2, 1);

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));

            inventoryService.reduceInventory(List.of(item1, item2));

            assertThat(product.getQuantity()).isEqualTo(8);
            assertThat(product2.getQuantity()).isEqualTo(4);
            verify(productRepository, times(2)).save(any());
        }
    }

    @Nested
    @DisplayName("RestoreInventory")
    class RestoreInventory {

        @Test
        @DisplayName("shouldRestoreInventorySuccessfully")
        void shouldRestoreInventorySuccessfully() {
            OrderItems orderItem = new OrderItems();
            orderItem.setProduct(product);
            orderItem.setQuantity(3);

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            inventoryService.restoreInventory(List.of(orderItem));

            assertThat(product.getQuantity()).isEqualTo(13);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("shouldThrowWhenProductNotFoundDuringRestore")
        void shouldThrowWhenProductNotFoundDuringRestore() {
            OrderItems orderItem = new OrderItems();
            orderItem.setProduct(product);
            orderItem.setQuantity(1);

            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.restoreInventory(List.of(orderItem)))
                    .isInstanceOf(InventoryException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("shouldRestoreInventoryForMultipleOrderItems")
        void shouldRestoreInventoryForMultipleOrderItems() {
            Product product2 = TestDataBuilder.buildProduct(new ObjectId());
            product2.setQuantity(5);

            OrderItems item1 = new OrderItems();
            item1.setProduct(product);
            item1.setQuantity(2);

            OrderItems item2 = new OrderItems();
            item2.setProduct(product2);
            item2.setQuantity(3);

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));

            inventoryService.restoreInventory(List.of(item1, item2));

            assertThat(product.getQuantity()).isEqualTo(12);
            assertThat(product2.getQuantity()).isEqualTo(8);
        }
    }
}