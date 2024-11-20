package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Entity extends Persistable {

    /**
     * Unique identifier, optimized for database queries and referencing.
     */
    @BsonId
    @NonNull
    private ObjectId id;

    /**
     * Human readable unique number for the instance
     */
    @NonNull
    private String reference;

    /**
     * Short description defining the instance.
     */
    @NonNull
    private String description;

    /**
     * Timestamp when the record was created in the database.
     */
    @NonNull
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated in the database.
     */
    @NonNull
    private LocalDateTime updatedAt;

}
