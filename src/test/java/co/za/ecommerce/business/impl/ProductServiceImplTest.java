package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.factories.DTOFactory;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTOCreateTest;
    private Product createProduct;

    @BeforeEach
    void setUp() {
        // Initialise ProductDTO
        productDTOCreateTest = DTOFactory.createProductDTO();

        // Initialize the Product
        createProduct = Product.builder()
                .description("description")
                .imageUrl("imageURl")
                .price(1.0)
                .category("category")
                .rate("4.5")
                .title("Product Title")
                .quantity("10")
                .build();
    }

    @Test
    void addProduct() {
//        when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(createProduct);
//
//        ProductDTO confirmProduct = productService.addProduct(productDTOCreateTest);
//
//        assertNotNull(confirmProduct);
    }
}