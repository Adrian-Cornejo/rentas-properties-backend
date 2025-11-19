package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.response.CloudinaryUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResponse uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);

    String extractPublicIdFromUrl(String imageUrl);
}