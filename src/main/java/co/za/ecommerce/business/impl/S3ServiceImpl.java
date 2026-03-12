package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.S3Service;
import co.za.ecommerce.exception.ProductException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file) {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
            log.info("File uploaded successfully to S3: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", e.getMessage());
            throw new ProductException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "Failed to upload image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", fileUrl);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage());
            throw new ProductException(
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "Failed to delete image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
