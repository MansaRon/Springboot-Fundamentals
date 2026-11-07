package co.za.ecommerce.dto.api;

import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Response for admin create/update of a single lookup value.
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class LookupValueApiResource extends GlobalApiResponse {
    private LookupValueDTO data;
}
