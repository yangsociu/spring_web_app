// Note: DTO định nghĩa dữ liệu gửi từ client khi Developer tạo game mới.
package com.gamehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class GameRequest {

    @NotBlank(message = "Tên game là bắt buộc")
    @Size(max = 100, message = "Tên game không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @Size(max = 1000, message = "Cấu hình yêu cầu không được vượt quá 1000 ký tự")
    private String requirements;

    private MultipartFile previewImage;

    private MultipartFile apkFile;

    private boolean supportLeaderboard;

    private boolean supportPoints;

    public GameRequest() {}

    public GameRequest(String name, String description, String requirements, MultipartFile previewImage,
                       MultipartFile apkFile, boolean supportLeaderboard, boolean supportPoints) {
        this.name = name;
        this.description = description;
        this.requirements = requirements;
        this.previewImage = previewImage;
        this.apkFile = apkFile;
        this.supportLeaderboard = supportLeaderboard;
        this.supportPoints = supportPoints;
    }

    // Getters và Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public MultipartFile getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(MultipartFile previewImage) {
        this.previewImage = previewImage;
    }

    public MultipartFile getApkFile() {
        return apkFile;
    }

    public void setApkFile(MultipartFile apkFile) {
        this.apkFile = apkFile;
    }

    public boolean isSupportLeaderboard() {
        return supportLeaderboard;
    }

    public void setSupportLeaderboard(boolean supportLeaderboard) {
        this.supportLeaderboard = supportLeaderboard;
    }

    public boolean isSupportPoints() {
        return supportPoints;
    }

    public void setSupportPoints(boolean supportPoints) {
        this.supportPoints = supportPoints;
    }
}
