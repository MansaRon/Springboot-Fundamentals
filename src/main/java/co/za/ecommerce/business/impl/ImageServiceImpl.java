package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.ImageService;
import co.za.ecommerce.dto.image.ImageDTO;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Image;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private GridFsTemplate gridFsTemplate;
    private GridFsOperations gridFsOperations;
    private ObjectMapper objectMapper;

    @Override
    public String addFile(MultipartFile file) throws IOException {
        DBObject dbObject = new BasicDBObject();
        dbObject.put("fileSize", file.getSize());

        Object fileID = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                dbObject
        );
        return fileID.toString();
    }

    @Override
    public ImageDTO downloadFile(String id) throws IOException {
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        Image image = new Image();
        if (gridFSFile != null && gridFSFile.getMetadata() != null) {
            image.setFileName(gridFSFile.getFilename());
            image.setFileType(gridFSFile.getMetadata().get("_contentType").toString());
            image.setFileSize(gridFSFile.getMetadata().get("fileSize").toString());
            image.setFile(IOUtils.toByteArray(
                    gridFsOperations.getResource(gridFSFile).getInputStream())
            );
        }
        return objectMapper.mapObject().map(image, ImageDTO.class);
    }
}
