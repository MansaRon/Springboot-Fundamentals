package co.za.ecommerce.business;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file);
    void deleteFile(String fileUrl);
}
