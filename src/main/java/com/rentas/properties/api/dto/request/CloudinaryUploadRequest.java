package com.rentas.properties.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for uploading image to Cloudinary")
public class CloudinaryUploadRequest {

    @Schema(description = "Folder name in Cloudinary", example = "rentmaster/organizations")
    private String folder;
}