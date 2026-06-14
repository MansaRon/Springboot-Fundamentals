package co.za.ecommerce.business;

import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.product.RatingDTO;
import co.za.ecommerce.model.Rating;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO, List<MultipartFile> imageFile);
    GetAllProductsDTO getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);
    ProductDTO getProduct(String id);
    // category
    GetAllProductsDTO getProductByCategory(String category, int pageNo, int pageSize, String sortBy, String sortDir);
    // description
    GetAllProductsDTO getProductByTitle(String title, int pageNo, int pageSize, String sortBy, String sortDir);
    List<ProductDTO> addMultipleProducts(List<ProductDTO> productDTOList, List<MultipartFile> imageFiles) throws IOException;
    ProductDTO updateProduct(String id, ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException;
    String deleteProduct(String id);
    String deleteAllProducts();
    RatingDTO addRating(RatingDTO rating, String productId, String userId);
    RatingDTO updateRating(RatingDTO rating, String productId, String userId);
    void deleteRating(RatingDTO rating, String productId, String userId);
}
