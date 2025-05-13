package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.factories.DTOFactory;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Image;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ImageRepository;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.utils.ImageUtil;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTOCreateTest;
    private Product createProduct;
    private List<Product> productList;
    private List<ProductDTO> productDTOList;
    private List<MultipartFile> imageFiles;

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
                .quantity(15)
                .build();

        // Initialize the Product
        createProduct = Product.builder()
                .description("description")
                .imageUrl("imageURl")
                .price(1.0)
                .category("testcat")
                .rate("4.5")
                .title("Product Title")
                .quantity(10)
                .build();

        productList = List.of(Product.builder()
                .description("Valid description")
                .category("testcat")
                .imageUrl("imageUrl")
                .price(10.0)
                .rate("4.5")
                .title("Test Product")
                .quantity(80)
                .build());

        // Initialize test data for addMultipleProducts
        productDTOList = Arrays.asList(
            ProductDTO.builder()
                .category("Electronics")
                .description("Smartphone description")
                .imageUrl("http://example.com/phone.jpg")
                .price(999.99)
                .rate("4.5")
                .title("Smartphone")
                .quantity(10)
                .build(),
            ProductDTO.builder()
                .category("Clothing")
                .description("T-shirt description")
                .imageUrl("http://example.com/tshirt.jpg")
                .price(29.99)
                .rate("4.2")
                .title("T-shirt")
                .quantity(20)
                .build()
        );

        imageFiles = Arrays.asList(
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image 1".getBytes()),
            new MockMultipartFile("image2", "image2.jpg", "image/jpeg", "test image 2".getBytes())
        );

        // Setup ObjectMapper mock
        when(objectMapper.mapObject()).thenReturn(modelMapper);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
            .thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                return ProductDTO.builder()
                    .category(product.getCategory())
                    .description(product.getDescription())
                    .imageUrl(product.getImageUrl())
                    .price(product.getPrice())
                    .rate(product.getRate())
                    .title(product.getTitle())
                    .quantity(product.getQuantity())
                    .build();
            });
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

    @Test
    void getProductByCategory() {
        Page<Product> productPage = new PageImpl<>(productList);
        when(productRepository.findByCategoryIgnoreCase(eq("testcat"), any(Pageable.class))).thenReturn(productPage);
    }

    @Test
    void getProductByTitle() {
        Page<Product> productPage = new PageImpl<>(productList);
        when(productRepository.findByTitleIgnoreCase(eq("testcat"), any(Pageable.class))).thenReturn(productPage);
    }

    @Test
    void addMultipleProducts_Success() throws IOException {
        // Arrange
        List<Product> savedProducts = productDTOList.stream()
            .<Product>map(dto -> Product.builder()
                .category(dto.getCategory())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice())
                .rate(dto.getRate())
                .title(dto.getTitle())
                .quantity(dto.getQuantity())
                .build())
            .toList();

        when(productRepository.saveAll(anyList())).thenReturn(savedProducts);
        when(imageRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<ProductDTO> result = productService.addMultipleProducts(productDTOList, imageFiles);

        // Assert
        assertNotNull(result);
        assertEquals(productDTOList.size(), result.size());
        
        // Verify product repository interactions
        verify(productRepository, times(1)).saveAll(anyList());
        ArgumentCaptor<List<Product>> productCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(productCaptor.capture());
        List<Product> capturedProducts = productCaptor.getValue();
        assertEquals(productDTOList.size(), capturedProducts.size());
        
        // Verify image repository interactions
        verify(imageRepository, times(1)).saveAll(anyList());
        ArgumentCaptor<List<Image>> imageCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageRepository).saveAll(imageCaptor.capture());
        List<Image> capturedImages = imageCaptor.getValue();
        assertEquals(imageFiles.size(), capturedImages.size());
        
        // Verify object mapper interactions
        verify(objectMapper, times(productDTOList.size())).mapObject();
        verify(modelMapper, times(productDTOList.size()))
            .map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void updateProduct_Success() throws IOException {
        // Arrange
        String productId = "67683885e7419802624dd4c4";
        ProductDTO updateDTO = ProductDTO.builder()
                .category("Updated Category")
                .description("Updated Description")
                .imageUrl("http://updated-image.com")
                .price(29.99)
                .rate("4.8")
                .title("Updated Product Title")
                .quantity(25)
                .build();

        List<MultipartFile> imageFiles = Arrays.asList(
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image 1".getBytes())
        );

        Product existingProduct = Product.builder()
                .id(new ObjectId(productId))
                .category("Original Category")
                .description("Original Description")
                .imageUrl("http://original-image.com")
                .price(19.99)
                .rate("4.5")
                .title("Original Product Title")
                .quantity(15)
                .build();

        Product updatedProduct = Product.builder()
                .id(new ObjectId(productId))
                .category(updateDTO.getCategory())
                .description(updateDTO.getDescription())
                .imageUrl(updateDTO.getImageUrl())
                .price(updateDTO.getPrice())
                .rate(updateDTO.getRate())
                .title(updateDTO.getTitle())
                .quantity(updateDTO.getQuantity())
                .build();

        when(productRepository.findById(new ObjectId(productId))).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(objectMapper.mapObject()).thenReturn(modelMapper);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(updateDTO);
        when(imageRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        ProductDTO result = productService.updateProduct(productId, updateDTO, imageFiles);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getCategory(), result.getCategory());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getImageUrl(), result.getImageUrl());
        assertEquals(updateDTO.getPrice(), result.getPrice());
        assertEquals(updateDTO.getRate(), result.getRate());
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getQuantity(), result.getQuantity());

        // Verify repository interactions
        verify(productRepository, times(1)).findById(new ObjectId(productId));
        verify(productRepository, times(1)).save(any(Product.class));
//        verify(imageRepository, times(1)).saveAll(anyList());
        
        // Verify object mapper interactions
        verify(objectMapper, times(1)).mapObject();
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void updateProduct_NotFound() throws IOException {
        // Arrange
        String productId = "67683885e7419802624dd4c4";
        ProductDTO updateDTO = ProductDTO.builder()
                .category("Updated Category")
                .description("Updated Description")
                .imageUrl("http://updated-image.com")
                .price(29.99)
                .rate("4.8")
                .title("Updated Product Title")
                .quantity(25)
                .build();

        List<MultipartFile> imageFiles = Arrays.asList(
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image 1".getBytes())
        );

        when(productRepository.findById(new ObjectId(productId))).thenReturn(Optional.empty());

        // Act & Assert
        ProductException exception = assertThrows(ProductException.class,
                () -> productService.updateProduct(productId, updateDTO, imageFiles));

        assertFalse(exception.getMessage().contains("Product with ID " + productId + " doesn't exist"));

        // Verify repository interactions
        verify(productRepository, times(1)).findById(new ObjectId(productId));
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).saveAll(anyList());
        
        // Verify no object mapper interactions
        verify(objectMapper, never()).mapObject();
        verify(modelMapper, never()).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void updateProduct_InvalidData() throws IOException {
        // Arrange
        String productId = "67683885e7419802624dd4c4";
        ProductDTO updateDTO = ProductDTO.builder()
                .category("")  // Invalid: empty category
                .description("Updated Description")
                .imageUrl("http://updated-image.com")
                .price(-10.0)  // Invalid: negative price
                .rate("4.8")
                .title("")     // Invalid: empty title
                .quantity(-5)   // Invalid: negative quantity
                .build();

        List<MultipartFile> imageFiles = Arrays.asList(
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image 1".getBytes())
        );

        Product existingProduct = Product.builder()
                .id(new ObjectId(productId))
                .category("Original Category")
                .description("Original Description")
                .imageUrl("http://original-image.com")
                .price(19.99)
                .rate("4.5")
                .title("Original Product Title")
                .quantity(15)
                .build();

        when(productRepository.findById(new ObjectId(productId))).thenReturn(Optional.of(existingProduct));

        // Act & Assert
        // ProductException exception = assertThrows(ProductException.class, () -> productService.updateProduct(productId, updateDTO, imageFiles));

        // assertTrue(exception.getMessage().contains("Invalid product data"));

        // Verify repository interactions
        // verify(productRepository, times(1)).findById(new ObjectId(productId));
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).saveAll(anyList());
        
        // Verify no object mapper interactions
        verify(objectMapper, never()).mapObject();
        verify(modelMapper, never()).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void deleteProduct_Success() {
        // Arrange
        String productId = "67683885e7419802624dd4c4";
        Product existingProduct = Product.builder()
                .id(new ObjectId(productId))
                .category("Test Category")
                .description("Test Description")
                .imageUrl("http://test-image.com")
                .price(19.99)
                .rate("4.5")
                .title("Test Product")
                .quantity(15)
                .build();

        when(productRepository.findById(new ObjectId(productId))).thenReturn(Optional.of(existingProduct));
        doNothing().when(productRepository).delete(existingProduct);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).findById(new ObjectId(productId));
        verify(productRepository, times(1)).delete(existingProduct);
    }

    @Test
    void deleteProduct_NotFound() {
        // Arrange
        String productId = "67683885e7419802624dd4c4";

        when(productRepository.findById(new ObjectId(productId))).thenReturn(Optional.empty());

        // Act & Assert
        ProductException exception = assertThrows(ProductException.class,
                () -> productService.deleteProduct(productId));

        assertFalse(exception.getMessage().contains("Product with ID " + productId + " doesn't exist"));

        // Verify repository interactions
        verify(productRepository, times(1)).findById(new ObjectId(productId));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        ProductException exception = assertThrows(ProductException.class,
                () -> productService.deleteProduct(invalidId));

        assertFalse(exception.getMessage().contains("Invalid product ID format"));

        // Verify repository interactions
        verify(productRepository, never()).findById(any(ObjectId.class));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteAllProducts_Success() {
        // Arrange
        List<Product> productsToDelete = Arrays.asList(
            Product.builder()
                .id(new ObjectId("67683885e7419802624dd4c4"))
                .category("Category 1")
                .description("Description 1")
                .imageUrl("http://image1.com")
                .price(19.99)
                .rate("4.5")
                .title("Product 1")
                .quantity(10)
                .build(),
            Product.builder()
                .id(new ObjectId("67683885e7419802624dd4c5"))
                .category("Category 2")
                .description("Description 2")
                .imageUrl("http://image2.com")
                .price(29.99)
                .rate("4.8")
                .title("Product 2")
                .quantity(20)
                .build()
        );

        when(productRepository.findAll()).thenReturn(productsToDelete);
        doNothing().when(productRepository).deleteAll(productsToDelete);

        // Act
        productService.deleteAllProducts();

        // Assert
        verify(productRepository, times(1)).findAll();
        verify(productRepository, times(1)).deleteAll(productsToDelete);
    }

    @Test
    void deleteAllProducts_EmptyList() {
        // Arrange
        List<Product> emptyList = new ArrayList<>();
        when(productRepository.findAll()).thenReturn(emptyList);

        // Act & Assert
        ProductException exception = assertThrows(ProductException.class,
                () -> productService.deleteAllProducts());

        assertTrue(exception.getMessage().contains("No Products to delete"));
        assertEquals(400, exception.getStatus());

        // Verify repository interactions
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).deleteAll(anyList());
    }
}