package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.response.CloudinaryUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Cloudinary", description = "Image upload and management with Cloudinary")
@SecurityRequirement(name = "bearerAuth")
public interface CloudinaryController {

    @Operation(
            summary = "Upload image",
            description = "Upload an image to Cloudinary and return the URL"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = CloudinaryUploadResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    ResponseEntity<CloudinaryUploadResponse> uploadImage(MultipartFile file, String folder);

    @Operation(
            summary = "Delete image",
            description = "Delete an image from Cloudinary by public ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    ResponseEntity<Void> deleteImage(String publicId);
}