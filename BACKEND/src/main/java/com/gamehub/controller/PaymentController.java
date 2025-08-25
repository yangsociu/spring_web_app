// Note: Controller xử lý API endpoints cho hệ thống thanh toán
// API endpoints: /api/v1/payment-info/*, /api/v1/deposits/*, /api/v1/withdraws/*
package com.gamehub.controller;

import com.gamehub.dto.DepositApprovalRequest;
import com.gamehub.dto.DepositResponse;
import com.gamehub.dto.PaymentInfoRequest;
import com.gamehub.dto.WithdrawApprovalRequest;
import com.gamehub.dto.WithdrawResponse;
import com.gamehub.model.DepositRequest;
import com.gamehub.model.PaymentInfo;
import com.gamehub.model.User;
import com.gamehub.model.WithdrawRequest;
import com.gamehub.service.CloudinaryService;
import com.gamehub.service.PaymentService;
import com.gamehub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ==================== ADMIN PAYMENT INFO MANAGEMENT ====================

    @PostMapping("/payment-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public ResponseEntity<?> createOrUpdatePaymentInfo(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("bankName") String bankName,
            @RequestParam("accountHolderName") String accountHolderName,
            @RequestParam(value = "qrCodeFile", required = false) MultipartFile qrCodeFile,
            Authentication authentication) {

        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Account number is required"));
            }
            if (bankName == null || bankName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Bank name is required"));
            }
            if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Account holder name is required"));
            }

            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            String qrCodeUrl = null;
            if (qrCodeFile != null && !qrCodeFile.isEmpty()) {
                String contentType = qrCodeFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(createErrorResponse("QR code file must be an image"));
                }
                if (qrCodeFile.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(createErrorResponse("QR code file size must be less than 5MB"));
                }
                try {
                    qrCodeUrl = cloudinaryService.uploadFile(qrCodeFile);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse("Failed to upload QR code image: " + e.getMessage()));
                }
            }

            PaymentInfoRequest request = new PaymentInfoRequest();
            request.setAccountNumber(accountNumber.trim());
            request.setBankName(bankName.trim());
            request.setAccountHolderName(accountHolderName.trim());
            request.setQrCodeUrl(qrCodeUrl);

            PaymentInfo paymentInfo;
            if (user.getRole().name().equals("ADMIN")) {
                paymentInfo = paymentService.createOrUpdatePaymentInfo(request);
            } else {
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse("User is required for non-admin payment info"));
                }
                paymentInfo = paymentService.createOrUpdatePaymentInfo(user, request);
            }

            return ResponseEntity.ok(paymentInfo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to save payment information: " + e.getMessage()));
        }
    }

    // ==================== USER PAYMENT INFO MANAGEMENT ====================

    @GetMapping("/payment-info/my-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public ResponseEntity<?> getMyPaymentInfo(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            Optional<PaymentInfo> paymentInfo = paymentService.getUserPaymentInfo(user);
            return paymentInfo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve payment information: " + e.getMessage()));
        }
    }

    @GetMapping("/payment-info")
    public ResponseEntity<?> getPaymentInfo() {
        try {
            Optional<PaymentInfo> paymentInfo = paymentService.getActivePaymentInfo();
            return paymentInfo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve payment information: " + e.getMessage()));
        }
    }

    // ==================== DEVELOPER DEPOSIT REQUESTS ====================

    @PostMapping("/deposits")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<?> createDepositRequest(
            @Valid @RequestBody com.gamehub.dto.DepositRequest request,
            Authentication authentication) {

        try {
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            DepositRequest depositRequest = paymentService.createDepositRequest(user, request);
            DepositResponse response = paymentService.convertToDepositResponse(depositRequest);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create deposit request: " + e.getMessage()));
        }
    }

    @GetMapping("/deposits/my-requests")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<?> getMyDepositRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        try {
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<DepositRequest> requests = paymentService.getUserDepositRequests(user, pageable);
            Page<DepositResponse> responseRequests = requests.map(paymentService::convertToDepositResponse);
            return ResponseEntity.ok(responseRequests);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve deposit requests: " + e.getMessage()));
        }
    }

    // ==================== ADMIN DEPOSIT APPROVAL ====================

    @GetMapping("/deposits/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingDepositRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            Page<DepositRequest> requests = paymentService.getPendingDepositRequests(pageable);
            Page<DepositResponse> responseRequests = requests.map(paymentService::convertToDepositResponse);
            return ResponseEntity.ok(responseRequests);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve pending deposits: " + e.getMessage()));
        }
    }

    @PutMapping("/deposits/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveDepositRequest(
            @PathVariable Long id,
            @Valid @RequestBody DepositApprovalRequest request,
            Authentication authentication) {

        try {
            User admin = userService.findByEmail(authentication.getName());
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("Admin user not found"));
            }

            DepositRequest depositRequest = paymentService.approveDepositRequest(id, admin, request);
            DepositResponse response = paymentService.convertToDepositResponse(depositRequest);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to approve deposit request: " + e.getMessage()));
        }
    }

    @GetMapping("/deposits/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DEVELOPER') and @paymentService.getDepositRequestById(#id).user.email == authentication.name)")
    public ResponseEntity<?> getDepositRequest(@PathVariable Long id) {
        try {
            DepositRequest depositRequest = paymentService.getDepositRequestById(id);
            DepositResponse response = paymentService.convertToDepositResponse(depositRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve deposit request: " + e.getMessage()));
        }
    }

    // ==================== DESIGNER WITHDRAW REQUESTS ====================

    @PostMapping("/withdraws")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<?> createWithdrawRequest(
            @Valid @RequestBody com.gamehub.dto.WithdrawRequest request,
            Authentication authentication) {

        try {
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            WithdrawRequest withdrawRequest = paymentService.createWithdrawRequest(user, request);
            WithdrawResponse response = paymentService.convertToWithdrawResponse(withdrawRequest);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create withdraw request: " + e.getMessage()));
        }
    }

    @GetMapping("/withdraws/my-requests")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<?> getMyWithdrawRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        try {
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not found"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<WithdrawRequest> requests = paymentService.getUserWithdrawRequests(user, pageable);
            Page<WithdrawResponse> responseRequests = requests.map(paymentService::convertToWithdrawResponse);
            return ResponseEntity.ok(responseRequests);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve withdraw requests: " + e.getMessage()));
        }
    }

    // ==================== ADMIN WITHDRAW APPROVAL ====================

    @GetMapping("/withdraws/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingWithdrawRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            Page<WithdrawRequest> requests = paymentService.getPendingWithdrawRequests(pageable);
            Page<WithdrawResponse> responseRequests = requests.map(paymentService::convertToWithdrawResponse);
            return ResponseEntity.ok(responseRequests);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve pending withdraws: " + e.getMessage()));
        }
    }

    @PutMapping("/withdraws/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveWithdrawRequest(
            @PathVariable Long id,
            @Valid @RequestBody WithdrawApprovalRequest request,
            Authentication authentication) {

        try {
            User admin = userService.findByEmail(authentication.getName());
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("Admin user not found"));
            }

            WithdrawRequest withdrawRequest = paymentService.approveWithdrawRequest(id, admin, request);
            WithdrawResponse response = paymentService.convertToWithdrawResponse(withdrawRequest);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to approve withdraw request: " + e.getMessage()));
        }
    }

    @GetMapping("/withdraws/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DESIGNER') and @paymentService.getWithdrawRequestById(#id).user.email == authentication.name)")
    public ResponseEntity<?> getWithdrawRequest(@PathVariable Long id) {
        try {
            WithdrawRequest withdrawRequest = paymentService.getWithdrawRequestById(id);
            WithdrawResponse response = paymentService.convertToWithdrawResponse(withdrawRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve withdraw request: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        error.put("timestamp", java.time.Instant.now().toString());
        return error;
    }
}
