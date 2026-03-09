package co.za.ecommerce.api;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.dto.api.ImageDTOApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Thendo
 * @date 2025/12/20
 */
public interface ImageAPI {

    @Operation(tags = "Images", summary = "Upload images")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = ImageDTOApiResource.class))
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    public ResponseEntity<ImageDTOApiResource> uploadImage(@Valid @RequestParam("image") MultipartFile file) throws IOException;

    @Operation(tags = "Images", summary = "Download images")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Image downloaded successfully",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request failed, incorrect payload",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authorised to access resource",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authorisation invalid",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request could not be completed",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    }),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema =
                                    @Schema(implementation = GlobalApiErrorResponse.class))
                    })
    })
    public ResponseEntity<?> download(@PathVariable String id) throws IOException;
}
