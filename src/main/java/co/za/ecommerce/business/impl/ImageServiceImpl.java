package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ImageService;
import co.za.ecommerce.dto.image.ImageDTO;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Image;
import co.za.ecommerce.repository.ImageRepository;
import co.za.ecommerce.utils.ImageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.zip.DataFormatException;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ImageDTO uploadFile(MultipartFile file) throws IOException {
        var imageSave = Image.builder()
                .createdAt(now())
                .updatedAt(now())
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(String.valueOf(file.getSize()))
                .file(ImageUtil.compressImage(file.getBytes()))
                .build();
        imageRepository.save(imageSave);
        return objectMapper.mapObject().map(imageSave, ImageDTO.class);
    }

    @Override
    public byte[] downloadFile(String fileId) {
        Optional<Image> dbImage = imageRepository.findById(new ObjectId(fileId));
        return dbImage.map(image -> {
            try {
                return ImageUtil.decompressImage(image.getFile());
            } catch (DataFormatException | IOException exception) {
                throw new ContextedRuntimeException
                        ("Error downloading an image", exception)
                        .addContextValue("Image ID", fileId)
                        .addContextValue("Image name", image.getFileName());
            }
        }).orElse(null);
    }
}
