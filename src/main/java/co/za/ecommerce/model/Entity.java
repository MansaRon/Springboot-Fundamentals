package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Entity extends Persistable {

    /**
     * Unique identifier, optimized for database queries and referencing.
     */
    @Id
    private ObjectId id;

    /**
     * Human readable unique number for the instance
     */
//    @NonNull
//    private String reference;

    /**
     * Short description defining the instance.
     */
//    @NonNull
//    private String description;

    /**
     * Timestamp when the record was created in the database.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated in the database.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public String hexId() {
        return id != null ? id.toHexString() : null;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

}
