package co.za.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server()
                .url("http://localhost:8080")
                .description("Localhost Server URL");

        Contact contact = new Contact()
                .email("kramashia101@gmail.com")
                .name("Thendo Ramashia");

        Info info = new Info()
                .title("Ecommerce API")
                .description("An ecommerce project for keeping up with Springboot ecosystem.")
                .termsOfService("terms")
                .contact(contact)
                .license(new License().name("GNU"))
                .version("1.0");

        return new OpenAPI().info(info).addServersItem(server);
    }
}
