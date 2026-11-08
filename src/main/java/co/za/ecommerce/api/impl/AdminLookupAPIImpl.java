package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.AdminLookupAPI;
import co.za.ecommerce.dto.api.ApiResource;
import co.za.ecommerce.dto.api.LookupValueApiResource;
import co.za.ecommerce.dto.api.LookupValueListApiResource;
import co.za.ecommerce.dto.lookup.LookupValueDTO;
import co.za.ecommerce.exception.LookupException;
import co.za.ecommerce.model.lookup.LookupType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static java.time.Instant.now;

/**
 * ADMIN only — CRUD for lookup reference data. Non-admin callers get 403.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/admin/lookup")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLookupAPIImpl extends API implements AdminLookupAPI {

    @Override
    @GetMapping("/{type}")
    public ResponseEntity<LookupValueListApiResource> getByType(@PathVariable String type) {
        log.info("ResponseEntity<LookupValueListApiResource> getByType(@PathVariable String type)");
        LookupType lookupType = parseType(type);
        return ResponseEntity.ok(
                LookupValueListApiResource.builder()
                        .timestamp(now())
                        .data(lookupService.getAllByType(lookupType))
                        .message("Lookup values retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PostMapping
    public ResponseEntity<LookupValueApiResource> create(@Valid @RequestBody LookupValueDTO request) {
        log.info("ResponseEntity<LookupValueApiResource> create(@Valid @RequestBody LookupValueDTO request)");
        return ResponseEntity.status(HttpStatus.CREATED).body(
                LookupValueApiResource.builder()
                        .timestamp(now())
                        .data(lookupService.create(request))
                        .message("Lookup value created.")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LookupValueApiResource> update(
            @PathVariable ObjectId id,
            @Valid @RequestBody LookupValueDTO request) {
        log.info("ResponseEntity<LookupValueApiResource> update(@PathVariable ObjectId id, @Valid @RequestBody LookupValueDTO request)");
        return ResponseEntity.ok(
                LookupValueApiResource.builder()
                        .timestamp(now())
                        .data(lookupService.update(id, request))
                        .message("Lookup value updated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResource> delete(@PathVariable ObjectId id) {
        log.info("ResponseEntity<ApiResource> delete(@PathVariable ObjectId id)");
        lookupService.delete(id);
        return ResponseEntity.ok(
                ApiResource.builder()
                        .timestamp(now())
                        .message("Lookup value deleted.")
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
