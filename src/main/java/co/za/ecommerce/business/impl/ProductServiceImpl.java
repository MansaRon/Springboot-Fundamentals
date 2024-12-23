package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ProductService;
import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.za.ecommerce.utils.DateUtil.now;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        Product saveProduct = Product.builder()
                .category(productDTO.getCategory())
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
        productRepository.save(saveProduct);
        return objectMapper.mapObject().map(saveProduct, ProductDTO.class);
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
    public GetAllProductsDTO searchPostsByKeyword(String keyword,
                                                  int pageNo,
                                                  int pageSize,
                                                  String sortBy,
                                                  String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();

        // create pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> getProducts = productRepository.findByProductKeyWord(keyword, pageable);

        //get content for page object
        List<Product> listOfProducts = getProducts.getContent().stream().toList();

        if (listOfProducts.isEmpty()) {
            throw new ProductException(HttpStatus.BAD_REQUEST.toString(), "No products with keyword " + keyword + " were found.", HttpStatus.BAD_REQUEST.value());
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
    public GetAllProductsDTO getProductByCategory(String category,
                                                 int pageNo,
                                                 int pageSize,
                                                 String sortBy,
                                                 String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // create pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> getProducts = productRepository.findByCategoryIgnoreCase(category, pageable);

        //get content for page object
        List<Product> listOfProducts = getProducts.getContent().stream().toList();

        if (listOfProducts.isEmpty()) {
            throw new ProductException(HttpStatus.BAD_REQUEST.toString(), "No products with category " + category + " were found.", HttpStatus.BAD_REQUEST.value());
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
}
