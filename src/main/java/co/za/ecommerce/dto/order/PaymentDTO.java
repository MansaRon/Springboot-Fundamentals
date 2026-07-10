package co.za.ecommerce.dto.order;

import co.za.ecommerce.dto.base.EntityDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String paymentMethod;
    private String paymentStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime paymentDate;
    private String transactionId;
}
