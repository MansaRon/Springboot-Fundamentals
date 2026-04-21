package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ProductService;
import co.za.ecommerce.business.S3Service;
import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;
import static co.za.ecommerce.utils.ValueUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    public ProductServiceImpl(ObjectMapper objectMapper,
                              ProductRepository productRepository,
                              S3Service s3Service) {
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.s3Service = s3Service;
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, List<MultipartFile> imageFiles) {
        List<String> imageUrls = uploadImages(imageFiles);
        Product product = Product.builder()
                .createdAt(now())
                .updatedAt(now())
                .category(productDTO.getCategory())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .rate(productDTO.getRate())
                .title(productDTO.getTitle())
                .quantity(productDTO.getQuantity())
                .imageUrls(!imageUrls.isEmpty() ? imageUrls : List.of())
                .build();
        Product savedProduct = productRepository.save(product);
        log.info("Product saved with id: {}", savedProduct.getId());

        return objectMapper.mapObject().map(savedProduct, ProductDTO.class);
    }

    @Override
    public GetAllProductsDTO getAllPosts(int pageNo,
                                         int pageSize,
                                         String sortBy,
                                         String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> getProducts = productRepository.findAll(pageable);

        if (getProducts.isEmpty()) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "No products found.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        return buildGetAllProductsDTO(getProducts);
    }

    @Override
    public ProductDTO getProduct(String id) {
        return objectMapper.mapObject().map(findProductById(id), ProductDTO.class);
    }

    @Override
    public GetAllProductsDTO getProductByCategory(String category,
                                                 int pageNo,
                                                 int pageSize,
                                                 String sortBy,
                                                 String sortDir) {
            log.info("Fetching products for category '{}' with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                    category, pageNo, pageSize, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

            Page<Product> getProducts = productRepository.findByCategoryIgnoreCase(category, pageable);

            if (getProducts.isEmpty()) {
                throw new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "No products with category '" + category + "' were found.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

        return buildGetAllProductsDTO(getProducts);
    }

    @Override
    public GetAllProductsDTO getProductByTitle(String title,
                                               int pageNo,
                                               int pageSize,
                                               String sortBy,
                                               String sortDir) {
            log.info("Fetching products for title '{}' with pageNo: {}, pageSize: {}, sortBy: {}, sortDir: {}",
                    title, pageNo, pageSize, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

            Page<Product> getProducts = productRepository.findByTitleIgnoreCase(title, pageable);

            if (getProducts.isEmpty()) {
                throw new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "No products with title '" + title + "' were found.",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

        return buildGetAllProductsDTO(getProducts);
    }

    @Override
    public List<ProductDTO> addMultipleProducts(List<ProductDTO> productDTOList, List<MultipartFile> imageFiles) {
        if (productDTOList == null || productDTOList.isEmpty()) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "The product list cannot be empty or null.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "The image list cannot be empty or null.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        int imagesPerProduct = imageFiles.size() / productDTOList.size();

        List<Product> productsToSave = new ArrayList<>();
        for (int i = 0; i < productDTOList.size(); i++) {
            ProductDTO productDTO = productDTOList.get(i);
            List<MultipartFile> productImages = imageFiles.subList(
                    i * imagesPerProduct,
                    (i + 1) * imagesPerProduct
            );

            List<String> imageUrls = uploadImages(productImages);

            productsToSave.add(Product.builder()
                    .category(productDTO.getCategory())
                    .createdAt(DateUtil.now())
                    .updatedAt(DateUtil.now())
                    .description(productDTO.getDescription())
                    .price(productDTO.getPrice())
                    .rate(productDTO.getRate())
                    .title(productDTO.getTitle())
                    .quantity(productDTO.getQuantity())
                    .imageUrls(imageUrls)
                    .build());
        }

        List<Product> savedProducts = productRepository.saveAll(productsToSave);

        return savedProducts.stream()
                .map(product -> objectMapper.mapObject().map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(String id, ProductDTO productDTO, List<MultipartFile> imageFiles) {
        Product productDB = findProductById(id);

        productDB.setId(productDB.getId());
        productDB.setUpdatedAt(now());
        productDB.setTitle(defaultIfNullOrEmpty(productDTO.getTitle(), productDB.getTitle()));
        productDB.setRate(defaultIfNullOrEmpty(productDTO.getRate(), productDB.getRate()));
        productDB.setDescription(defaultIfNullOrEmpty(productDTO.getDescription(), productDB.getDescription()));
        productDB.setPrice(defaultIfNullOrZero(productDTO.getPrice(), productDB.getPrice()));
        productDB.setQuantity(defaultIfNullOrZero(productDTO.getQuantity(), productDB.getQuantity()));
        productDB.setCategory(defaultIfNullOrEmpty(productDTO.getCategory(), productDB.getCategory()));

        if (imageFiles != null && !imageFiles.isEmpty()) {
            if (!productDB.getImageUrls().isEmpty()) {
                productDB.getImageUrls().forEach(s3Service::deleteFile);
            }
            productDB.setImageUrls(uploadImages(imageFiles));
        }

        Product savedProduct = productRepository.save(productDB);
        return objectMapper.mapObject().map(savedProduct, ProductDTO.class);
    }

    @Override
    public String deleteProduct(String id) {
        Product product = productRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product with id '" + id + "' not found.",
                        HttpStatus.BAD_REQUEST.value()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            product.getImageUrls().forEach(s3Service::deleteFile);
        }

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

        findAllProducts.forEach(product -> {
            if (product.getImageUrls() != null) {
                product.getImageUrls().forEach(s3Service::deleteFile);
            }
        });

        productRepository.deleteAll(findAllProducts);
        return "All products and their associated images were deleted.";
    }

    private Product findProductById(String id) {
        if (checkNullId(id)) {
            throw new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid ID format. ID must be a 24-character hexadecimal string.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
        return productRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ProductException(
                        HttpStatus.BAD_REQUEST.toString(),
                        "Product with ID " + id + " doesn't exist.",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    private List<String> uploadImages(List<MultipartFile> imageFiles) {
        return imageFiles.stream()
                .map(s3Service::uploadFile)
                .collect(Collectors.toList());
    }

    private GetAllProductsDTO buildGetAllProductsDTO(Page<Product> page) {
        List<ProductDTO> productDTOs = page.getContent()
                .stream()
                .map(product -> objectMapper.mapObject().map(product, ProductDTO.class))
                .toList();

        return GetAllProductsDTO.builder()
                .products(productDTOs)
                .pageNo(page.getNumber())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private boolean checkNullId(String Id) {
        return Id == null || !Id.matches("^[a-fA-F0-9]{24}$");
    }
}
