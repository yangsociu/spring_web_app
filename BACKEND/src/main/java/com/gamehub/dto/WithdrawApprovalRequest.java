package com.gamehub.dto;

import com.gamehub.model.enums.WithdrawStatus;
import jakarta.validation.constraints.NotNull;

public class WithdrawApprovalRequest {

    @NotNull(message = "Status is required")
    private WithdrawStatus status;

    private String adminNote;

    public WithdrawApprovalRequest() {}

    public WithdrawApprovalRequest(WithdrawStatus status, String adminNote) {
        this.status = status;
        this.adminNote = adminNote;
    }

    public WithdrawStatus getStatus() {
        return status;
    }

    public void setStatus(WithdrawStatus status) {
        this.status = status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}
