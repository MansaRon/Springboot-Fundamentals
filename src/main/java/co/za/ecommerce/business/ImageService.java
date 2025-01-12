package co.za.ecommerce.business;

import co.za.ecommerce.dto.image.ImageDTO;
import co.za.ecommerce.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    String addFile(MultipartFile file) throws IOException;
    ImageDTO downloadFile(String id) throws IOException;
}
