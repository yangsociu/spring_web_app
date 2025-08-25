package com.gamehub.dto;
public class GiftRedeemRequest {
    private Long playerId;
    private Long giftId;
    // Getters và setters
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public Long getGiftId() { return giftId; }
    public void setGiftId(Long giftId) { this.giftId = giftId; }
}
