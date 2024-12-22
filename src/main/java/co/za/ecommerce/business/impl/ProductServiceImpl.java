package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ProductService;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static co.za.ecommerce.utils.DateUtil.now;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        return objectMapper.mapObject().map(
                Product.builder()
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
                        .build(), ProductDTO.class
        );
    }
}
