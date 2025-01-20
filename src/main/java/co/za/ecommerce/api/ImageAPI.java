package co.za.ecommerce.api;

import co.za.ecommerce.dto.api.ImageDTOApiResource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/image")
public class ImageAPI extends API {

    @PermitAll
    @PostMapping("/upload")
    public ResponseEntity<ImageDTOApiResource> uploadImage(
            @Valid
            @RequestParam("image")MultipartFile file) throws IOException {
        log.trace("public ResponseEntity<ImageDTOApiResource> uploadImage(@Valid @RequestParam(\"file\")MultipartFile file) throws IOException");
        return ResponseEntity.ok(
                ImageDTOApiResource.builder()
                        .timestamp(now())
                        .data(imageService.uploadFile(file))
                        .message("Image Uploaded.")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PermitAll
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable String id) throws IOException {
        // Need to find a way to rewrite this method to confine with the rest of the APIs
        byte[] image = imageService.downloadFile(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_GIF_VALUE))
                .body(image);
    }
}
