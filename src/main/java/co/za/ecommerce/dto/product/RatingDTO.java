package co.za.ecommerce.dto.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class RatingDTO {

    private String userId;

    private LocalDateTime reviewDate;

    @NotBlank(message = "Username is required")
    private String userName;

    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private double rating;

    private String comment;
}
