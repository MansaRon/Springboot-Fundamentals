package co.za.ecommerce.api;

import co.za.ecommerce.mapper.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class API {

    @Autowired
    protected ObjectMapper mapper;
}
