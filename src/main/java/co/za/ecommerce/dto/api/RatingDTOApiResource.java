package co.za.ecommerce.dto.api;

import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.product.RatingDTO;
import co.za.ecommerce.model.Rating;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class RatingDTOApiResource extends GlobalApiResponse {
    private RatingDTO data;
    private List<RatingDTO> reviews;
}
