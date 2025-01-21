package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ProductService;
import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.exception.ResourceNotFoundException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Image;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ImageRepository;
import co.za.ecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException {
        Product product = Product.builder()
                .createdAt(now())
                .updatedAt(now())
                .category(productDTO.getCategory())
                .description(productDTO.getDescription())
                .imageUrl(productDTO.getImageUrl())
                .price(productDTO.getPrice())
                .rate(productDTO.getRate())
                .title(productDTO.getTitle())
                .quantity(productDTO.getQuantity())
                .build();
        Product savedProduct = productRepository.save(product);

        List<Image> multipleImages = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            Image image = Image.builder()
                    .createdAt(now())
                    .updatedAt(now())
                    .fileName(imageFile.getName())
                    .fileSize(String.valueOf(imageFile.getSize()))
                    .fileType(imageFile.getContentType())
                    .file(imageFile.getBytes())
                    .product(savedProduct)
                    .build();
            Image savedImage = imageRepository.save(image);
            multipleImages.add(savedImage);
        }

        savedProduct.setImages(multipleImages);
        productRepository.save(savedProduct);

        return objectMapper.mapObject().map(savedProduct, ProductDTO.class);
    }

    @Override
    public GetAllProductsDTO getAllPosts(int pageNo,
                                         int pageSize,
                                         String sortBy,
                                         String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> getProducts = productRepository.findAll(pageable);

        //get content for page object
        List<Product> listOfProducts = getProducts.getContent().stream().toList();

        if (listOfProducts.isEmpty()) {
            throw new ProductException(HttpStatus.BAD_REQUEST.toString(), "No products found", HttpStatus.BAD_REQUEST.value());
        }

        List<ProductDTO> productDTOs =
                listOfProducts
                        .stream()
                        .map(mapProduct -> objectMapper.mapObject().map(mapProduct, ProductDTO.class))
                        .toList();

        return GetAllProductsDTO.builder()
                .products(productDTOs)
                .pageNo(getProducts.getTotalPages())
                .totalElements(getProducts.getTotalElements())
                .totalPages(getProducts.getTotalPages())
                .last(getProducts.isLast())
                .build();
    }

    @Override
    public ProductDTO getProduct(String id) {
        // Validate the ID format
        if (id == null || !id.matches("^[a-fA-F0-9]{24}$")) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid ID format. ID must be a 24-character hexadecimal string.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Product findProduct = productRepository.findById(new ObjectId(id))
                .orElseThrow(() ->
                        new ProductException(
                                HttpStatus.BAD_REQUEST.toString(),
                                "Product with ID " + id + " doesn't exist.",
                                HttpStatus.BAD_REQUEST.value()
                        ));
        return objectMapper.mapObject().map(findProduct, ProductDTO.class);
    }

    @Override
    public GetAllProductsDTO getProductByCategory(String category,
                                                 int pageNo,
                                                 int pageSize,
                                                 String sortBy,
                                                 String sortDir) {
        try {
            log.info("Fetching products for category '{}' with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                    category, pageNo, pageSize, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

            // Attempt to retrieve products by category
            Page<Product> getProducts = productRepository.findByCategoryIgnoreCase(category, pageable);

            // Handle empty product list
            if (getProducts.isEmpty()) {
                throw new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "No products with category '" + category + "' were found.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // Map the products to DTOs
            List<ProductDTO> productDTOs = getProducts.getContent()
                    .stream()
                    .map(mapProduct -> objectMapper.mapObject().map(mapProduct, ProductDTO.class))
                    .toList();

            // Build and return the response DTO
            return GetAllProductsDTO.builder()
                    .products(productDTOs)
                    .pageNo(getProducts.getNumber())
                    .totalElements(getProducts.getTotalElements())
                    .totalPages(getProducts.getTotalPages())
                    .last(getProducts.isLast())
                    .build();

        } catch (org.springframework.data.mapping.PropertyReferenceException e) {
            // Catch property reference errors and rethrow as custom exceptions
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid property reference in query: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
        } catch (Exception e) {
            // Handle unexpected exceptions gracefully
            throw new ProductException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "An unexpected error occurred while retrieving products: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    @Override
    public GetAllProductsDTO getProductByTitle(String title,
                                               int pageNo,
                                               int pageSize,
                                               String sortBy,
                                               String sortDir) {
        try {
            log.info("Fetching products for title '{}' with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                    title, pageNo, pageSize, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

            // Attempt to retrieve products by category
            Page<Product> getProducts = productRepository.findByTitleIgnoreCase(title, pageable);

            // Handle empty product list
            if (getProducts.isEmpty()) {
                throw new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "No products with title '" + title + "' were found.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // Map the products to DTOs
            List<ProductDTO> productDTOs = getProducts.getContent()
                    .stream()
                    .map(mapProduct -> objectMapper.mapObject().map(mapProduct, ProductDTO.class))
                    .toList();

            // Build and return the response DTO
            return GetAllProductsDTO.builder()
                    .products(productDTOs)
                    .pageNo(getProducts.getNumber())
                    .totalElements(getProducts.getTotalElements())
                    .totalPages(getProducts.getTotalPages())
                    .last(getProducts.isLast())
                    .build();
        } catch (org.springframework.data.mapping.PropertyReferenceException e) {
            // Catch property reference errors and rethrow as custom exceptions
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid property reference in query: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public List<ProductDTO> addMultipleProducts(List<ProductDTO> productDTOList) {
        // Check if the input list is null or empty
        if (productDTOList == null || productDTOList.isEmpty()) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "The product list cannot be empty or null.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        List<Product> productsToSave = productDTOList.stream()
                .map(productDTO -> Product.builder()
                        .category(productDTO.getCategory())
                        .createdAt(now())
                        .updatedAt(now())
                        .description(productDTO.getDescription())
                        .imageUrl(productDTO.getImageUrl())
                        .price(productDTO.getPrice())
                        .rate(productDTO.getRate())
                        .title(productDTO.getTitle())
                        .quantity(productDTO.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Save all products at once
        List<Product> savedProducts = productRepository.saveAll(productsToSave);

        // Map saved products to DTOs
        return savedProducts.stream()
                .map(product -> objectMapper.mapObject().map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        if (id == null || !id.matches("^[a-fA-F0-9]{24}$")) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid ID format. ID must be a 24-character hexadecimal string.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Product productDB = productRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product with id '" + id + "' not found.",
                        HttpStatus.BAD_REQUEST.value()));

        productDB.setTitle(productDTO.getTitle());
        productDB.setRate(productDTO.getRate());
        productDB.setDescription(productDTO.getDescription());
        productDB.setPrice(productDTO.getPrice());
        productDB.setQuantity(productDTO.getQuantity());
        productDB.setUpdatedAt(now());
        productDB.setCategory(productDTO.getCategory());
        productDB.setUpdatedAt(productDTO.getUpdatedAt());

        Product saveProduct = productRepository.save(productDB);

        return objectMapper.mapObject().map(saveProduct, ProductDTO.class);
    }

    @Override
    public String deleteProduct(String id) {
        if (id == null || !id.matches("^[a-fA-F0-9]{24}$")) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid ID format. ID must be a 24-character hexadecimal string.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Product product = productRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product with id '" + id + "' not found.",
                        HttpStatus.BAD_REQUEST.value()));
        productRepository.delete(product);
        return "Item with ID " + id + " was deleted.";
    }

    @Override
    public String deleteAllProducts() {
        List<Product> findAllProducts = productRepository.findAll();
        if (findAllProducts.isEmpty()) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "No Products to delete",
                    HttpStatus.BAD_REQUEST.value());
        }
        productRepository.deleteAll(findAllProducts);
        return "All products were deleted.";
    }
}
