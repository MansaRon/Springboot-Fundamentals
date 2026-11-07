package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.LookupAPI;
import co.za.ecommerce.dto.api.LookupValueListApiResource;
import co.za.ecommerce.exception.LookupException;
import co.za.ecommerce.model.lookup.LookupType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Instant.now;

/**
 * PUBLIC (no auth) — reference data the frontend renders option lists from.
 * Registered as permit-all in SecurityConfig.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lookup")
public class LookupAPIImpl extends API implements LookupAPI {

    @Override
    @GetMapping("/{type}")
    public ResponseEntity<LookupValueListApiResource> getByType(@PathVariable String type) {
        log.info("ResponseEntity<LookupValueListApiResource> getByType(@PathVariable String type)");
        LookupType lookupType = parseType(type);
        return ResponseEntity.ok(
                LookupValueListApiResource.builder()
                        .timestamp(now())
                        .data(lookupService.getActiveByType(lookupType))
                        .message("Lookup values retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private LookupType parseType(String type) {
        try {
            return LookupType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new LookupException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Invalid lookup type: " + type,
                    HttpStatus.BAD_REQUEST.value());
        }
    }
}
