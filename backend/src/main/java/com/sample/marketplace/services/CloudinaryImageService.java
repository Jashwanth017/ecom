package com.sample.marketplace.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sample.marketplace.config.CloudinaryProperties;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryImageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryImageService(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    public String uploadProductImage(MultipartFile image) {
        validateImage(image);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap(
                            "folder", properties.productFolder(),
                            "resource_type", "image",
                            "public_id", UUID.randomUUID().toString(),
                            "overwrite", false
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (!(secureUrl instanceof String url) || url.isBlank()) {
                throw new IllegalStateException("Cloudinary did not return an image URL");
            }
            return url;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to upload image to Cloudinary", exception);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Product image is required");
        }
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Product image must be 5 MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image uploads are allowed");
        }
    }
}
