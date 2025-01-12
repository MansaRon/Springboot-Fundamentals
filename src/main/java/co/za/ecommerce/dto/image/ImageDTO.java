package co.za.ecommerce.dto.image;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO extends EntityDTO {

    @NotNull
    private String fileName;

    @NotNull
    private String fileType;

    @NotNull
    private String fileSize;

    @NotNull
    private byte[] file;
}
