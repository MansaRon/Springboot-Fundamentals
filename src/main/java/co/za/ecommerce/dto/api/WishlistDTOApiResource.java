package co.za.ecommerce.dto.api;

import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.user.ResetPwdDTO;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
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
public class WishlistDTOApiResource extends GlobalApiResponse {
    private WishlistDTO data;
    private List<WishlistDTO> dataList;
    private String dataDelete;
}
