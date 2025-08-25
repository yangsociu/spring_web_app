package com.gamehub.controller;

import com.gamehub.model.User;
import com.gamehub.model.enums.UserRole;
import com.gamehub.model.enums.UserStatus;
import com.gamehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody Map<String, Object> profileData, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            // Update avatarUrl if provided
            if (profileData.containsKey("avatarUrl")) {
                user.setAvatarUrl((String) profileData.get("avatarUrl"));
            }

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getUserBalance(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Map<String, Object> response = new HashMap<>();
            // Convert BigDecimal to double for JSON response
            response.put("balance", user.getBalance().doubleValue());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("balance", 0.0);
            errorResponse.put("error", "Could not fetch balance: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/developers")
    public ResponseEntity<List<User>> getAllDevelopers() {
        try {
            List<User> developers = userService.findByRoleAndStatus(UserRole.DEVELOPER, UserStatus.APPROVED);
            return ResponseEntity.ok(developers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
