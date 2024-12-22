package co.za.ecommerce.business;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.model.Product;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);
}
