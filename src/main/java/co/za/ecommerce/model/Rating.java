package co.za.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Rating {

    @NotNull
    private String userId;

    @NotNull
    private LocalDateTime reviewDate;

    @NotNull
    private String userName;

    @NotNull
    @Size(min = 1, max = 10)
    private double rating;

    private String comment;
}
