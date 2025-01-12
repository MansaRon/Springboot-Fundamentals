package co.za.ecommerce.business;

import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;

import java.util.List;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);
    GetAllProductsDTO getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);
    ProductDTO getProduct(String id);
    // category
    GetAllProductsDTO getProductByCategory(String category, int pageNo, int pageSize, String sortBy, String sortDir);
    // description
    GetAllProductsDTO getProductByTitle(String title, int pageNo, int pageSize, String sortBy, String sortDir);
    List<ProductDTO> addMultipleProducts(List<ProductDTO> productDTOList);
    ProductDTO updateProduct(String id, ProductDTO productDTO);
    String deleteProduct(String id);
    String deleteAllProducts();
}
