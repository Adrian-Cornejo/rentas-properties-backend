package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after uploading image to Cloudinary")
public class CloudinaryUploadResponse {

    @Schema(description = "Public ID of the uploaded image in Cloudinary", example = "rentmaster/organizations/abc123")
    private String publicId;

    @Schema(description = "Secure URL of the uploaded image", example = "https://res.cloudinary.com/demo/image/upload/v1234567890/rentmaster/organizations/abc123.jpg")
    private String url;

    @Schema(description = "Image format", example = "jpg")
    private String format;

    @Schema(description = "Image width in pixels", example = "800")
    private Integer width;

    @Schema(description = "Image height in pixels", example = "600")
    private Integer height;
}