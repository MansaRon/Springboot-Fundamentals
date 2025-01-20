package co.za.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Blob;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "images")
public class Image extends Entity {

    /**
     * Name of the image.
     */
    @NotNull
    private String fileName;

    /**
     * Type of the image (JPG, IMG...).
     */
    @NotNull
    private String fileType;

    /**
     * Instance of image.
     */
    @NotNull
    private String fileSize;

    /**
     * Download URL.
     */
    @NotNull
    private byte[] file;

    /**
     * Product associated with the product.
     */
//    @DBRef
//    @NotNull
//    private Product product;
}
