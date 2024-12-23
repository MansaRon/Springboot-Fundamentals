package co.za.ecommerce.business;

import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;

import java.util.List;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);
    GetAllProductsDTO getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);
    ProductDTO getProduct(String id);
    GetAllProductsDTO searchPostsByKeyword(String keyword, int pageNo, int pageSize, String sortBy, String sortDir);
    // category
    GetAllProductsDTO getProductByCategory(String category, int pageNo, int pageSize, String sortBy, String sortDir);
    // description
    // price
    // rating
    // title
    // quantity
}
