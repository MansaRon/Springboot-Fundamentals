package co.za.ecommerce.business;

import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.model.Product;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);
    GetAllProductsDTO getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);
}
