package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "refresh_token")
public class RefreshToken extends Entity {

    /**
     * User token.
     */
    @NonNull
    private String token;

    /**
     * User ID for refresh token.
     */
    @NonNull
    private String userId;

    /**
     * Expiry date for refresh token.
     */
    @NonNull
    private Date expiry;

}
