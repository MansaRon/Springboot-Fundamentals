package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import factory.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartItemsMapper Tests")
class CartItemsMapperTest {

    private final CartItemsMapper mapper = new CartItemsMapper();

    @Test
    @DisplayName("shouldReturnNullWhenCartItemsIsNull")
    void shouldReturnNullWhenCartItemsIsNull() {
        assertThat(CartItemsMapper.toDTO(null)).isNull();
    }

    @Test
    @DisplayName("shouldMapCartItemsToDTO")
    void shouldMapCartItemsToDTO() {
        Product product = TestDataBuilder.buildProduct();
        CartItems item = TestDataBuilder.buildCartItem(product, 3);

        CartItemsDTO dto = CartItemsMapper.toDTO(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getQuantity()).isEqualTo(3);
        assertThat(dto.getProductDTO()).isNotNull();
        assertThat(dto.getProductDTO().getTitle()).isEqualTo("Organic Fleece Hoodie");
    }

    @Test
    @DisplayName("shouldReturnNullListWhenInputListIsNull")
    void shouldReturnNullListWhenInputListIsNull() {
        assertThat(mapper.toDTOList(null)).isNull();
    }

    @Test
    @DisplayName("shouldMapListOfCartItemsToDTOList")
    void shouldMapListOfCartItemsToDTOList() {
        Product product = TestDataBuilder.buildProduct();
        CartItems item = TestDataBuilder.buildCartItem(product, 2);

        List<CartItemsDTO> dtos = mapper.toDTOList(List.of(item));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getQuantity()).isEqualTo(2);
    }
}
