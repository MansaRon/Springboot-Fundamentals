package co.za.ecommerce.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PaymentDetails {
    private String paymentMethod;
    private String paymentStatus;
    @CreatedDate
    private LocalDateTime paymentDate;
    private String transactionId;
}
