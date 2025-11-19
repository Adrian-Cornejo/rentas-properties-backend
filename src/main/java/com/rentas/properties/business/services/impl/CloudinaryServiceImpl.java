package com.rentas.properties.business.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rentas.properties.api.dto.response.CloudinaryUploadResponse;
import com.rentas.properties.api.exception.CloudinaryDeleteException;
import com.rentas.properties.api.exception.CloudinaryUploadException;
import com.rentas.properties.api.exception.InvalidImageFormatException;
import com.rentas.properties.business.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public CloudinaryUploadResponse uploadImage(MultipartFile file, String folder) {
        log.info("Starting image upload - filename: {}, size: {}, folder: {}",
                file.getOriginalFilename(), file.getSize(), folder);

        validateImage(file);

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "transformation", new com.cloudinary.Transformation()
                            .width(800)
                            .height(800)
                            .crop("limit")
                            .quality("auto")
                            .fetchFormat("auto")
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            Integer width = (Integer) uploadResult.get("width");
            Integer height = (Integer) uploadResult.get("height");

            log.info("Image uploaded successfully - publicId: {}, url: {}", publicId, url);

            return CloudinaryUploadResponse.builder()
                    .publicId(publicId)
                    .url(url)
                    .format(format)
                    .width(width)
                    .height(height)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new CloudinaryUploadException("Error uploading image to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {
        log.info("Starting image deletion - publicId: {}", publicId);

        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            if (!"ok".equals(resultStatus)) {
                log.warn("Image deletion returned non-ok status - publicId: {}, status: {}",
                        publicId, resultStatus);
                throw new CloudinaryDeleteException("Failed to delete image: " + resultStatus);
            }

            log.info("Image deleted successfully - publicId: {}", publicId);

        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary - publicId: {}", publicId, e);
            throw new CloudinaryDeleteException("Error deleting image from Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // URL format: https://res.cloudinary.com/[cloud_name]/image/upload/v[version]/[folder]/[public_id].[format]
            // or: https://res.cloudinary.com/[cloud_name]/image/upload/[folder]/[public_id].[format]

            if (!imageUrl.contains("cloudinary.com")) {
                return null;
            }

            // Split by /upload/ to get everything after it
            String[] uploadSplit = imageUrl.split("/upload/");
            if (uploadSplit.length < 2) {
                return null;
            }

            String afterUpload = uploadSplit[1];

            // Remove version if present (v1234567890/)
            afterUpload = afterUpload.replaceFirst("v\\d+/", "");

            // Remove file extension
            int lastDot = afterUpload.lastIndexOf('.');
            if (lastDot > 0) {
                afterUpload = afterUpload.substring(0, lastDot);
            }

            log.info("Extracted publicId: {} from URL: {}", afterUpload, imageUrl);
            return afterUpload;

        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
            return null;
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFormatException("File size exceeds maximum allowed size of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidImageFormatException("File name cannot be null");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_FORMATS.contains(extension)) {
            throw new InvalidImageFormatException("Invalid file format. Allowed formats: " + ALLOWED_FORMATS);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageFormatException("File must be an image");
        }
    }

    
}