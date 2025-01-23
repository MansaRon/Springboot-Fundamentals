package co.za.ecommerce.api;

import co.za.ecommerce.dto.api.WishlistDTOApiResource;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/wishlist")
public class WishlistApi extends API {

    //Security alternatives
    //@Secured({"USER"})
    //@PreAuthorize("hasRole('ADMIN')")
    @PermitAll
    @PostMapping()
    public ResponseEntity<WishlistDTOApiResource> addWishlist(@RequestBody WishlistDTO wishlistDTO) {
        log.trace("public ResponseEntity<WishlistDTOApiResource> addItemToWishlist(@RequestBody WishlistDTO wishlistDTO)");
        return ResponseEntity.ok(
                WishlistDTOApiResource.builder()
                        .timestamp(now())
                        .data(wishlistService.add(wishlistDTO))
                        .message("Wishlist item added.")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    //@Secured({"USER"})
    //@PreAuthorize("hasRole('ADMIN')")
    // To refactor to have pagination
    @PermitAll
    @GetMapping()
    public ResponseEntity<WishlistDTOApiResource> getWishlist(@RequestBody String userID) {
        log.trace("public ResponseEntity<WishlistDTOApiResource> getWishlist(@RequestBody String userID)");
        return ResponseEntity.ok(
                WishlistDTOApiResource.builder()
                        .timestamp(now())
                        .dataList(wishlistService.findAll(userID))
                        .message("Wishlist retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    //@Secured({"USER"})
    //@PreAuthorize("hasRole('ADMIN')")
    @PermitAll
    @DeleteMapping("/{userID}")
    public ResponseEntity<WishlistDTOApiResource> deleteWishlist(@PathVariable String userID, @RequestBody WishlistDTO wishlistDTO) {
        log.trace("public ResponseEntity<WishlistDTOApiResource> deleteWishlist(@RequestBody String userID, @RequestBody WishlistDTO wishlistDTO)");
        return ResponseEntity.ok(
                WishlistDTOApiResource.builder()
                        .timestamp(now())
                        .dataDelete(wishlistService.delete(userID, wishlistDTO))
                        .message("Wishlist item deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
