// Note: Controller xử lý các API liên quan đến duyệt game bởi Admin.
package com.gamehub.controller;

import com.gamehub.dto.GameApprovalRequest;
import com.gamehub.exception.GameException;
import com.gamehub.service.GameApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class GameApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(GameApprovalController.class);

    @Autowired
    private GameApprovalService gameApprovalService;

    @PostMapping("/game-approve")
    public ResponseEntity<String> approveGame(@Valid @RequestBody GameApprovalRequest approvalRequest) throws GameException {
        logger.info("Game approval request for game ID: {}", approvalRequest.getGameId());
        gameApprovalService.approveGame(approvalRequest);
        return ResponseEntity.ok("Game approval status updated successfully");
    }
}
