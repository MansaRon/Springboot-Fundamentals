package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.factories.DTOFactory;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        // Initialise ProductDTO
        productDTOCreateTest = ProductDTO.builder()
                .category("Category test")
                .description("Category Description")
                .imageUrl("http:image.com")
                .price(15.00)
                .rate("4.5")
                .title("Product Title")
                .quantity("15")
                .build();

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
        when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(createProduct);
    }

    @Test
    void getProduct() {
        String validID = "67683885e7419802624dd4c4";

        when(productRepository.findById(new ObjectId(validID)))
                .thenReturn(Optional.empty());

        ProductException exception = assertThrows(ProductException.class,
                () -> productService.getProduct(validID));

        assertTrue(exception.getMessage().contains("Product with ID 67683885e7419802624dd4c4 doesn't exist"));
    }
}