package co.za.ecommerce.dto.api;

import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Response for the public lookup GET. Per the frontend contract the list is
 * carried under {@code data} (not {@code dataList}).
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class LookupValueListApiResource extends GlobalApiResponse {
    private List<LookupValueDTO> data;
}
