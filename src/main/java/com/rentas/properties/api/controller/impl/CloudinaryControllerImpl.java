package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.CloudinaryController;
import com.rentas.properties.api.dto.response.CloudinaryUploadResponse;
import com.rentas.properties.business.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/cloudinary")
@RequiredArgsConstructor
public class CloudinaryControllerImpl implements CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @Override
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CloudinaryUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "rentmaster") String folder) {

        log.info("Uploading image to Cloudinary - folder: {}, originalFilename: {}",
                folder, file.getOriginalFilename());

        CloudinaryUploadResponse response = cloudinaryService.uploadImage(file, folder);

        log.info("Image uploaded successfully - publicId: {}, url: {}",
                response.getPublicId(), response.getUrl());

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/delete/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteImage(@PathVariable String publicId) {
        log.info("Deleting image from Cloudinary - publicId: {}", publicId);

        cloudinaryService.deleteImage(publicId);

        log.info("Image deleted successfully - publicId: {}", publicId);

        return ResponseEntity.ok().build();
    }
}