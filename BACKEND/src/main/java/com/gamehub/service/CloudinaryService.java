// Dịch vụ xử lý tải tệp lên Cloudinary và trả về URL của tệp đã tải,
// xử lý lỗi nếu tệp rỗng hoặc tải lên thất bại.
package com.gamehub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gamehub.exception.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.error("Upload failed: File is null or empty");
            throw new GameException("File is required for upload.");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.error("Upload failed: File size {} bytes exceeds limit of {} bytes", file.getSize(), MAX_FILE_SIZE);
            throw new GameException("File size must be less than 50MB.");
        }

        try {
            // Determine resource type based on content type
            Map<String, Object> options = new HashMap<>();
            String contentType = file.getContentType();
            logger.info("Uploading file: name={}, type={}", file.getOriginalFilename(), contentType);

            if (contentType != null && contentType.startsWith("audio/")) {
                options.put("resource_type", "video"); // MP3/audio treated as video in Cloudinary
            } else {
                options.put("resource_type", "image"); // EPS or other images
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("File uploaded successfully: URL={}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new GameException("Error uploading file to Cloudinary: " + e.getMessage(), e);
        }
    }

    public String generateJpgPreview(MultipartFile epsFile) {
        if (epsFile == null || epsFile.isEmpty()) {
            logger.error("Preview generation failed: EPS file is null or empty");
            throw new GameException("EPS file is required for preview generation.");
        }

        // Validate file size
        if (epsFile.getSize() > MAX_FILE_SIZE) {
            logger.error("Preview generation failed: EPS file size {} bytes exceeds limit of {} bytes",
                    epsFile.getSize(), MAX_FILE_SIZE);
            throw new GameException("EPS file size must be less than 50MB.");
        }

        try {
            logger.info("Generating JPG preview for EPS file: name={}", epsFile.getOriginalFilename());
            // Upload EPS file with transformation to JPG format
            Map uploadResult = cloudinary.uploader().upload(epsFile.getBytes(),
                    ObjectUtils.asMap(
                            "format", "jpg",
                            "quality", "auto:good",
                            "width", 800,
                            "height", 600,
                            "crop", "limit",
                            "resource_type", "image"
                    ));
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("JPG preview generated successfully: URL={}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            logger.error("Error generating JPG preview from EPS file: {}", e.getMessage(), e);
            throw new GameException("Error generating JPG preview from EPS file: " + e.getMessage(), e);
        }
    }
}
