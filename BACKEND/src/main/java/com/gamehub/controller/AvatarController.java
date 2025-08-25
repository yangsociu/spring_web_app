package com.gamehub.controller;

import com.gamehub.service.CloudinaryService;
import com.gamehub.service.UserService;
import com.gamehub.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/avatar")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AvatarController {

    private static final Logger logger = LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('DESIGNER')")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            Authentication authentication) {

        logger.info("Avatar upload request by user: {}", authentication.getName());

        try {
            String avatarUrl = cloudinaryService.uploadFile(avatarFile);

            User user = userService.findByEmail(authentication.getName());
            user.setAvatarUrl(avatarUrl);
            userService.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            response.put("message", "Avatar uploaded successfully");

            logger.info("Avatar uploaded successfully for user: {} - URL: {}", authentication.getName(), avatarUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading avatar for user: {}", authentication.getName(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/my-avatar")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('DESIGNER')")
    public ResponseEntity<Map<String, String>> getMyAvatar(Authentication authentication) {
        logger.info("Get avatar request by user: {}", authentication.getName());

        try {
            User user = userService.findByEmail(authentication.getName());
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", user.getAvatarUrl());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting avatar for user: {}", authentication.getName(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
