// Note: Service xử lý logic nghiệp vụ cho hệ thống thanh toán
// Chức năng: Quản lý thông tin thanh toán admin, xử lý yêu cầu nạp tiền developer
package com.gamehub.service;

import com.gamehub.dto.DepositApprovalRequest;
import com.gamehub.dto.DepositResponse;
import com.gamehub.dto.PaymentInfoRequest;
import com.gamehub.dto.WithdrawApprovalRequest;
import com.gamehub.dto.WithdrawResponse;
import com.gamehub.exception.ResourceNotFoundException;
import com.gamehub.model.DepositRequest;
import com.gamehub.model.PaymentInfo;
import com.gamehub.model.User;
import com.gamehub.model.WithdrawRequest;
import com.gamehub.model.enums.DepositStatus;
import com.gamehub.model.enums.WithdrawStatus;
import com.gamehub.repository.DepositRequestRepository;
import com.gamehub.repository.PaymentInfoRepository;
import com.gamehub.repository.UserRepository;
import com.gamehub.repository.WithdrawRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentInfoRepository paymentInfoRepository;

    @Autowired
    private WithdrawRequestRepository withdrawRequestRepository;

    @Autowired
    private DepositRequestRepository depositRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PaymentInfo createOrUpdatePaymentInfo(PaymentInfoRequest request) {
        // Tìm system user
        User systemUser = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new IllegalStateException("System admin user not found"));

        // Sửa query để tìm theo system user thay vì null
        Optional<PaymentInfo> existingInfo = paymentInfoRepository.findSystemActivePaymentInfo();

        PaymentInfo paymentInfo;
        if (existingInfo.isPresent()) {
            paymentInfo = existingInfo.get();
            paymentInfo.setAccountNumber(request.getAccountNumber());
            paymentInfo.setBankName(request.getBankName());
            paymentInfo.setAccountHolderName(request.getAccountHolderName());
            if (request.getQrCodeUrl() != null) {
                paymentInfo.setQrCodeUrl(request.getQrCodeUrl());
            }
        } else {
            // Deactivate existing system infos
            List<PaymentInfo> existingInfos = paymentInfoRepository.findSystemPaymentInfos();
            existingInfos.forEach(info -> {
                info.setIsActive(false);
                paymentInfoRepository.save(info);
            });

            paymentInfo = new PaymentInfo(
                    systemUser, // Sử dụng systemUser
                    request.getAccountNumber(),
                    request.getBankName(),
                    request.getAccountHolderName(),
                    request.getQrCodeUrl()
            );
        }

        return paymentInfoRepository.save(paymentInfo);
    }

    // Payment Info Management (User-specific)
    @Transactional
    public PaymentInfo createOrUpdatePaymentInfo(User user, PaymentInfoRequest request) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for user-specific payment info");
        }

        Optional<PaymentInfo> existingInfo = paymentInfoRepository.findByUserAndIsActiveTrue(user);

        PaymentInfo paymentInfo;
        if (existingInfo.isPresent()) {
            paymentInfo = existingInfo.get();
            paymentInfo.setAccountNumber(request.getAccountNumber());
            paymentInfo.setBankName(request.getBankName());
            paymentInfo.setAccountHolderName(request.getAccountHolderName());
            if (request.getQrCodeUrl() != null) {
                paymentInfo.setQrCodeUrl(request.getQrCodeUrl());
            }
        } else {
            // Deactivate any existing payment info for this user
            List<PaymentInfo> existingInfos = paymentInfoRepository.findByUser(user);
            existingInfos.forEach(info -> {
                info.setIsActive(false);
                paymentInfoRepository.save(info);
            });

            paymentInfo = new PaymentInfo(
                    user,
                    request.getAccountNumber(),
                    request.getBankName(),
                    request.getAccountHolderName(),
                    request.getQrCodeUrl()
            );
        }

        return paymentInfoRepository.save(paymentInfo);
    }

    public Optional<PaymentInfo> getUserPaymentInfo(User user) {
        return paymentInfoRepository.findByUserAndIsActiveTrue(user);
    }

    // Sửa getActivePaymentInfo
    public Optional<PaymentInfo> getActivePaymentInfo() {
        return paymentInfoRepository.findSystemActivePaymentInfo();
    }

    // Deposit Request Management
    public DepositRequest createDepositRequest(User user, com.gamehub.dto.DepositRequest request) {
        DepositRequest depositRequest = new DepositRequest(
                user,
                new BigDecimal(request.getAmount()),
                request.getTransactionNote()
        );

        return depositRequestRepository.save(depositRequest);
    }

    public Page<DepositRequest> getPendingDepositRequests(Pageable pageable) {
        return depositRequestRepository.findByStatus(DepositStatus.PENDING, pageable);
    }

    public Page<DepositRequest> getUserDepositRequests(User user, Pageable pageable) {
        return depositRequestRepository.findByUser(user, pageable);
    }

    @Transactional
    public DepositRequest approveDepositRequest(Long requestId, User admin, DepositApprovalRequest approvalRequest) {
        DepositRequest depositRequest = depositRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit request not found"));

        if (depositRequest.getStatus() != DepositStatus.PENDING) {
            throw new IllegalStateException("Deposit request is not in pending status");
        }

        depositRequest.setStatus(approvalRequest.getStatus());
        depositRequest.setAdminNote(approvalRequest.getAdminNote());
        depositRequest.setApprovedBy(admin);
        depositRequest.setApprovedAt(LocalDateTime.now());

        // Nếu duyệt thành công, cộng tiền vào tài khoản user
        if (approvalRequest.getStatus() == DepositStatus.APPROVED) {
            User user = depositRequest.getUser();
            user.setBalance(user.getBalance().add(depositRequest.getAmount()));
            userRepository.save(user);
        }

        return depositRequestRepository.save(depositRequest);
    }

    public DepositRequest getDepositRequestById(Long id) {
        return depositRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit request not found"));
    }

    public DepositResponse convertToDepositResponse(DepositRequest depositRequest) {
        DepositResponse response = new DepositResponse();
        response.setId(depositRequest.getId());
        response.setUserId(depositRequest.getUser().getId());
        response.setUserEmail(depositRequest.getUser().getEmail());
        response.setUserName(depositRequest.getUser().getFullName());
        response.setAmount(depositRequest.getAmount());
        response.setStatus(depositRequest.getStatus());
        response.setTransactionNote(depositRequest.getTransactionNote());
        response.setAdminNote(depositRequest.getAdminNote());

        if (depositRequest.getApprovedBy() != null) {
            response.setApprovedById(depositRequest.getApprovedBy().getId());
            response.setApprovedByEmail(depositRequest.getApprovedBy().getEmail());
        }

        response.setApprovedAt(depositRequest.getApprovedAt());
        response.setCreatedAt(depositRequest.getCreatedAt());
        response.setUpdatedAt(depositRequest.getUpdatedAt());

        return response;
    }

    // Withdraw Request Management
    @Transactional
    public WithdrawRequest createWithdrawRequest(User user, com.gamehub.dto.WithdrawRequest request) {
        // Validate user has sufficient balance
        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal");
        }

        // Get user's payment info
        Optional<PaymentInfo> paymentInfo = getUserPaymentInfo(user);
        if (paymentInfo.isEmpty()) {
            throw new IllegalArgumentException("Payment information is required for withdrawal");
        }

        PaymentInfo info = paymentInfo.get();
        WithdrawRequest withdrawRequest = new WithdrawRequest(
                user,
                request.getAmount(),
                request.getUserNote(),
                info.getBankName(),
                info.getAccountNumber(),
                info.getAccountHolderName()
        );

        return withdrawRequestRepository.save(withdrawRequest);
    }

    public Page<WithdrawRequest> getPendingWithdrawRequests(Pageable pageable) {
        return withdrawRequestRepository.findByStatus(WithdrawStatus.PENDING, pageable);
    }

    public Page<WithdrawRequest> getUserWithdrawRequests(User user, Pageable pageable) {
        return withdrawRequestRepository.findByUser(user, pageable);
    }

    @Transactional
    public WithdrawRequest approveWithdrawRequest(Long requestId, User admin, WithdrawApprovalRequest approvalRequest) {
        WithdrawRequest withdrawRequest = withdrawRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdraw request not found"));

        if (withdrawRequest.getStatus() != WithdrawStatus.PENDING) {
            throw new IllegalStateException("Withdraw request is not in pending status");
        }

        withdrawRequest.setStatus(approvalRequest.getStatus());
        withdrawRequest.setAdminNote(approvalRequest.getAdminNote());
        withdrawRequest.setApprovedBy(admin);
        withdrawRequest.setApprovedAt(LocalDateTime.now());

        // If approved, deduct money from user account
        if (approvalRequest.getStatus() == WithdrawStatus.APPROVED) {
            User user = withdrawRequest.getUser();

            // Double-check balance before deduction
            if (user.getBalance().compareTo(withdrawRequest.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient balance for withdrawal");
            }

            user.setBalance(user.getBalance().subtract(withdrawRequest.getAmount()));
            userRepository.save(user);
        }

        return withdrawRequestRepository.save(withdrawRequest);
    }

    public WithdrawRequest getWithdrawRequestById(Long id) {
        return withdrawRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Withdraw request not found"));
    }

    public WithdrawResponse convertToWithdrawResponse(WithdrawRequest withdrawRequest) {
        WithdrawResponse response = new WithdrawResponse();
        response.setId(withdrawRequest.getId());
        response.setUserId(withdrawRequest.getUser().getId());
        response.setUserEmail(withdrawRequest.getUser().getEmail());
        response.setUserName(withdrawRequest.getUser().getFullName());
        response.setAmount(withdrawRequest.getAmount());
        response.setStatus(withdrawRequest.getStatus());
        response.setUserNote(withdrawRequest.getUserNote());
        response.setAdminNote(withdrawRequest.getAdminNote());
        response.setBankName(withdrawRequest.getBankName());
        response.setAccountNumber(withdrawRequest.getAccountNumber());
        response.setAccountHolderName(withdrawRequest.getAccountHolderName());

        if (withdrawRequest.getApprovedBy() != null) {
            response.setApprovedById(withdrawRequest.getApprovedBy().getId());
            response.setApprovedByEmail(withdrawRequest.getApprovedBy().getEmail());
        }

        response.setApprovedAt(withdrawRequest.getApprovedAt());
        response.setCreatedAt(withdrawRequest.getCreatedAt());
        response.setUpdatedAt(withdrawRequest.getUpdatedAt());

        return response;
    }
}
