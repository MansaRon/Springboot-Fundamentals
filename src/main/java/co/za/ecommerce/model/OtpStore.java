package co.za.ecommerce.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp")
public class OtpStore {

    @Id
    private String phoneNumber; // Could be email, phone, or UUID

    @NonNull
    private String otp;

    @NonNull
    private LocalDateTime expiryTime;

    private int retryAttempts;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
