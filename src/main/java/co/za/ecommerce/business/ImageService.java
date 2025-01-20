package co.za.ecommerce.business;

import co.za.ecommerce.dto.image.ImageDTO;
import co.za.ecommerce.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    ImageDTO uploadFile(MultipartFile file) throws IOException;
    byte[] downloadFile(String id) throws IOException;
}
