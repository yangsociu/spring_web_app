package com.gamehub.service;

import com.gamehub.dto.GiftDTO;
import com.gamehub.dto.GiftTransactionDTO;
import com.gamehub.exception.GameException;
import com.gamehub.model.Gift;
import com.gamehub.model.GiftTransaction;
import com.gamehub.model.PlayerDeveloperPoints;
import com.gamehub.model.User;
import com.gamehub.repository.GiftRepository;
import com.gamehub.repository.GiftTransactionRepository;
import com.gamehub.repository.PlayerDeveloperPointsRepository;
import com.gamehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GiftService {

    private static final Logger logger = LoggerFactory.getLogger(GiftService.class);

    @Autowired
    private GiftRepository giftRepository;

    @Autowired
    private GiftTransactionRepository giftTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService; // Bổ sung: Để kiểm tra total_points

    @Autowired
    private PlayerDeveloperPointsRepository playerDeveloperPointsRepository; // Bổ sung: Để cập nhật total_points

    @Autowired
    private CloudinaryService cloudinaryService;

    public GiftDTO uploadGift(Long developerId, String name, String description, MultipartFile imageFile, Long pointCost, Long quantity) throws GameException {
        logger.info("Uploading gift: DeveloperID={}, Name={}, PointCost={}, Quantity={}", developerId, name, pointCost, quantity);

        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> {
                    logger.warn("Developer not found: {}", developerId);
                    return new GameException("Developer not found");
                });

        if (pointCost <= 0) {
            logger.warn("Invalid point cost: {}", pointCost);
            throw new GameException("Point cost must be greater than 0");
        }

        if (quantity < 0) {
            logger.warn("Invalid quantity: {}", quantity);
            throw new GameException("Quantity cannot be negative");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(imageFile);
                logger.info("Image uploaded successfully for gift: {}", imageUrl);
            } catch (Exception e) {
                logger.error("Error uploading image for gift: {}", name, e);
                throw new GameException("Failed to upload image: " + e.getMessage());
            }
        }

        Gift gift = new Gift(name, description, imageUrl, pointCost, quantity, developer, LocalDateTime.now());
        gift = giftRepository.save(gift);

        logger.info("Gift uploaded successfully: GiftID={}", gift.getId());
        return new GiftDTO(
                gift.getId(),
                gift.getName(),
                gift.getDescription(),
                gift.getImageUrl(),
                gift.getPointCost(),
                gift.getQuantity(),
                gift.getDeveloper().getId(),
                gift.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    public List<GiftDTO> getAllGifts() {
        logger.info("Fetching all gifts");
        return giftRepository.findAll().stream()
                .map(gift -> new GiftDTO(
                        gift.getId(),
                        gift.getName(),
                        gift.getDescription(),
                        gift.getImageUrl(),
                        gift.getPointCost(),
                        gift.getQuantity(),
                        gift.getDeveloper().getId(),
                        gift.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .collect(Collectors.toList());
    }

    public GiftTransactionDTO redeemGift(Long playerId, Long giftId) throws GameException {
        logger.info("Redeeming gift: PlayerID={}, GiftID={}", playerId, giftId);

        User player = userRepository.findById(playerId)
                .orElseThrow(() -> {
                    logger.warn("Player not found: {}", playerId);
                    return new GameException("Player not found");
                });

        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> {
                    logger.warn("Gift not found: {}", giftId);
                    return new GameException("Gift not found");
                });

        if (gift.getQuantity() <= 0) {
            logger.warn("Gift out of stock: GiftID={}", giftId);
            throw new GameException("Gift is out of stock");
        }

        // Trừ điểm từ PlayerDeveloperPoints
        PlayerDeveloperPoints pdp = playerDeveloperPointsRepository.findByPlayerAndDeveloper(player, gift.getDeveloper())
                .orElseThrow(() -> new GameException("Player points not found for this developer"));
        pdp.setTotalPoints(pdp.getTotalPoints() - gift.getPointCost());
        pdp.setLastUpdated(LocalDateTime.now());
        playerDeveloperPointsRepository.save(pdp);

        // Giảm quantity của gift
        gift.setQuantity(gift.getQuantity() - 1);
        giftRepository.save(gift);

        // Tạo giao dịch đổi quà
        GiftTransaction transaction = new GiftTransaction(player, gift, gift.getPointCost(), LocalDateTime.now());
        transaction = giftTransactionRepository.save(transaction);

        logger.info("Gift redeemed successfully: TransactionID={}", transaction.getId());
        return new GiftTransactionDTO(
                transaction.getId(),
                transaction.getPlayer().getId(),
                transaction.getGift().getId(),
                transaction.getPointsSpent(),
                transaction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    public List<GiftDTO> getGiftsByDeveloper(Long developerId) {
        logger.info("Fetching gifts for developer: {}", developerId);
        return giftRepository.findByDeveloperId(developerId).stream()
                .map(gift -> new GiftDTO(
                        gift.getId(),
                        gift.getName(),
                        gift.getDescription(),
                        gift.getImageUrl(),
                        gift.getPointCost(),
                        gift.getQuantity(),
                        gift.getDeveloper().getId(),
                        gift.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .collect(Collectors.toList());
    }

    public List<GiftDTO> getMyGifts(String email) throws GameException {
        logger.info("Fetching gifts for developer email: {}", email);

        User developer = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Developer not found with email: {}", email);
                    return new GameException("Developer not found");
                });

        return giftRepository.findByDeveloperId(developer.getId()).stream()
                .map(gift -> new GiftDTO(
                        gift.getId(),
                        gift.getName(),
                        gift.getDescription(),
                        gift.getImageUrl(),
                        gift.getPointCost(),
                        gift.getQuantity(),
                        gift.getDeveloper().getId(),
                        gift.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .collect(Collectors.toList());
    }

    public List<GiftTransactionDTO> getGiftTransactions(Long playerId) throws GameException {
        logger.info("Fetching gift transactions for PlayerID={}", playerId);
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> {
                    logger.warn("Player not found: {}", playerId);
                    return new GameException("Player not found");
                });

        return giftTransactionRepository.findByPlayerId(playerId).stream()
                .map(transaction -> new GiftTransactionDTO(
                        transaction.getId(),
                        transaction.getPlayer().getId(),
                        transaction.getGift().getId(),
                        transaction.getPointsSpent(),
                        transaction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .collect(Collectors.toList());
    }

    public GiftDTO updateGift(Long id, String name, String description, MultipartFile imageFile, Long pointCost, Long quantity) throws GameException {
        logger.info("Updating gift: GiftID={}, Name={}, PointCost={}, Quantity={}", id, name, pointCost, quantity);

        Gift gift = giftRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Gift not found: {}", id);
                    return new GameException("Gift not found");
                });

        if (name != null && !name.trim().isEmpty()) {
            gift.setName(name);
        }

        if (description != null) {
            gift.setDescription(description);
        }

        if (pointCost != null) {
            if (pointCost <= 0) {
                logger.warn("Invalid point cost: {}", pointCost);
                throw new GameException("Point cost must be greater than 0");
            }
            gift.setPointCost(pointCost);
        }

        if (quantity != null) {
            if (quantity < 0) {
                logger.warn("Invalid quantity: {}", quantity);
                throw new GameException("Quantity cannot be negative");
            }
            gift.setQuantity(quantity);
        }

        // Cập nhật hình ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                gift.setImageUrl(imageUrl);
                logger.info("Image updated successfully for gift: {}", imageUrl);
            } catch (Exception e) {
                logger.error("Error updating image for gift: {}", name, e);
                throw new GameException("Failed to update image: " + e.getMessage());
            }
        }

        gift = giftRepository.save(gift);

        logger.info("Gift updated successfully: GiftID={}", gift.getId());
        return new GiftDTO(
                gift.getId(),
                gift.getName(),
                gift.getDescription(),
                gift.getImageUrl(),
                gift.getPointCost(),
                gift.getQuantity(),
                gift.getDeveloper().getId(),
                gift.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    public void deleteGift(Long id, Boolean force) throws GameException {
        logger.info("Deleting gift: GiftID={}, Force={}", id, force);

        Gift gift = giftRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Gift not found: {}", id);
                    return new GameException("Gift not found");
                });

        if (!Boolean.TRUE.equals(force)) {
            try {
                List<GiftTransaction> transactions = giftTransactionRepository.findByGiftId(id);
                if (transactions != null && !transactions.isEmpty()) {
                    logger.warn("Cannot delete gift with existing transactions: GiftID={}, TransactionCount={}", id, transactions.size());
                    throw new GameException("Cannot delete gift that has been redeemed by players. Found " + transactions.size() + " transaction(s). Use force=true to delete anyway.");
                }
            } catch (Exception e) {
                logger.warn("Could not check gift transactions for GiftID={}, proceeding with deletion: {}", id, e.getMessage());
            }
        } else {
            logger.info("Force deleting gift with potential transactions: GiftID={}", id);
        }

        giftRepository.delete(gift);
        logger.info("Gift deleted successfully: GiftID={}", id);
    }

    public void deleteGift(Long id) throws GameException {
        deleteGift(id, false);
    }
}
