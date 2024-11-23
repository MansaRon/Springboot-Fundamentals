package co.za.ecommerce.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapper {
    public ModelMapper mapObject() {
        return new ModelMapper();
    }
}
