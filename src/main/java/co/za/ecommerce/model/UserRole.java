package co.za.ecommerce.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class UserRole {
    @NonNull
    private String code;

    @NonNull
    private String description;
}
