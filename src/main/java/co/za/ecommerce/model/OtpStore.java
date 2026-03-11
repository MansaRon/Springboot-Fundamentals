package co.za.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp")
public class OtpStore extends Entity {

    @Indexed(unique = true)
    private String phoneNumber;

    @NotNull
    private String otp;

    @NotNull
    private LocalDateTime expiryTime;

    private int retryAttempts;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
