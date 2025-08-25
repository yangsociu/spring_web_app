package com.gamehub.controller;

import com.gamehub.dto.GiftDTO;
import com.gamehub.dto.GiftTransactionDTO;
import com.gamehub.exception.GameException;
import com.gamehub.service.GiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gifts")
public class GiftController {

    private static final Logger logger = LoggerFactory.getLogger(GiftController.class);

    @Autowired
    private GiftService giftService;

    @PostMapping("/upload")
    public ResponseEntity<GiftDTO> uploadGift(
            @RequestParam Long developerId,
            @RequestParam(required = false) String name, // Made name parameter optional to prevent MissingServletRequestParameterException
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) Long pointCost, // Made pointCost optional to prevent MissingServletRequestParameterException
            @RequestParam(required = false) Long quantity, // Made quantity optional to prevent MissingServletRequestParameterException
            Authentication authentication) throws GameException {
        logger.info("Upload gift request: DeveloperID={}, Name={}, Quantity={}", developerId, name, quantity);

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("DEVELOPER"))) {
            logger.warn("Unauthorized gift upload attempt: DeveloperID={}", developerId);
            throw new GameException("Unauthorized");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new GameException("Gift name is required");
        }

        if (pointCost == null) {
            throw new GameException("Point cost is required");
        }

        if (quantity == null) {
            throw new GameException("Quantity is required");
        }

        GiftDTO giftDTO = giftService.uploadGift(developerId, name, description, imageFile, pointCost, quantity);
        return new ResponseEntity<>(giftDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GiftDTO>> getAllGifts() {
        logger.info("Fetching all gifts");
        List<GiftDTO> gifts = giftService.getAllGifts();
        return new ResponseEntity<>(gifts, HttpStatus.OK);
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<GiftDTO>> getGiftsByDeveloper(@PathVariable Long developerId) {
        logger.info("Fetching gifts for developer: {}", developerId);
        List<GiftDTO> gifts = giftService.getGiftsByDeveloper(developerId);
        return new ResponseEntity<>(gifts, HttpStatus.OK);
    }

    @GetMapping("/my-gifts")
    public ResponseEntity<List<GiftDTO>> getMyGifts(Authentication authentication) throws GameException {
        logger.info("Fetching gifts for authenticated developer: {}", authentication.getName());

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("DEVELOPER"))) {
            logger.warn("Unauthorized my-gifts fetch attempt: {}", authentication != null ? authentication.getName() : "null");
            throw new GameException("Unauthorized");
        }

        List<GiftDTO> gifts = giftService.getMyGifts(authentication.getName());
        return new ResponseEntity<>(gifts, HttpStatus.OK);
    }

    @PostMapping("/redeem")
    public ResponseEntity<GiftTransactionDTO> redeemGift(
            @RequestParam Long playerId,
            @RequestParam Long giftId,
            Authentication authentication) throws GameException {
        logger.info("Redeem gift request: PlayerID={}, GiftID={}", playerId, giftId);

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("PLAYER"))) {
            logger.warn("Unauthorized gift redeem attempt: PlayerID={}", playerId);
            throw new GameException("Unauthorized");
        }

        GiftTransactionDTO transactionDTO = giftService.redeemGift(playerId, giftId);
        return new ResponseEntity<>(transactionDTO, HttpStatus.OK);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<GiftTransactionDTO>> getGiftTransactions(
            @RequestParam Long playerId,
            Authentication authentication) throws GameException {
        logger.info("Fetch gift transactions request: PlayerID={}", playerId);

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("PLAYER"))) {
            logger.warn("Unauthorized gift transactions fetch attempt: PlayerID={}", playerId);
            throw new GameException("Unauthorized");
        }

        List<GiftTransactionDTO> transactions = giftService.getGiftTransactions(playerId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GiftDTO> updateGift(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) Long pointCost,
            @RequestParam(required = false) Long quantity,
            Authentication authentication) throws GameException {
        logger.info("Update gift request: GiftID={}, Name={}, PointCost={}, Quantity={}", id, name, pointCost, quantity);

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("DEVELOPER"))) {
            logger.warn("Unauthorized gift update attempt: GiftID={}", id);
            throw new GameException("Unauthorized");
        }

        // Cho phép update từng phần (partial update)
        GiftDTO giftDTO = giftService.updateGift(id, name, description, imageFile, pointCost, quantity);
        return new ResponseEntity<>(giftDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGift(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") Boolean force, // Added force parameter to allow deletion of gifts with transactions
            Authentication authentication) throws GameException {
        logger.info("Delete gift request: GiftID={}, Force={}", id, force);

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("DEVELOPER"))) {
            logger.warn("Unauthorized gift delete attempt: GiftID={}", id);
            throw new GameException("Unauthorized");
        }

        giftService.deleteGift(id, force); // Pass force parameter to service
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
