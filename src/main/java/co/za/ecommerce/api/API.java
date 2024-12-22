package co.za.ecommerce.api;

import co.za.ecommerce.business.ProductService;
import co.za.ecommerce.business.UserService;
import co.za.ecommerce.mapper.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class API {

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ProductService productService;
}
