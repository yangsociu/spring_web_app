// Note: Xử lý logic tạo và quản lý game do Developer upload.
package com.gamehub.service;

import com.gamehub.dto.GameRequest;
import com.gamehub.dto.GameResponse;
import com.gamehub.exception.GameException;
import com.gamehub.model.Game;
import com.gamehub.model.User;
import com.gamehub.model.enums.GameStatus;
import com.gamehub.model.enums.UserRole;
import com.gamehub.model.enums.UserStatus;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository,
                       @Value("${file.upload-dir}") String uploadDir) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.UPLOAD_DIR = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
    }

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    // Giả lập lưu trữ file, thay bằng dịch vụ lưu trữ thực tế (S3, local storage, v.v.)
    private final String UPLOAD_DIR;

    public GameResponse createGame(GameRequest gameRequest, String userEmail) throws GameException {
        logger.info("Game creation attempt by user: {}", userEmail);

        User developer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userEmail);
                    return new GameException("User not found");
                });

        if (developer.getRole() != UserRole.DEVELOPER) {
            logger.warn("Game creation failed - user is not a DEVELOPER: {}", userEmail);
            throw new GameException("Only DEVELOPER accounts can create games");
        }

        if (developer.getStatus() != UserStatus.APPROVED) {
            logger.warn("Game creation failed - user is not approved: {}", userEmail);
            throw new GameException("Your account is not approved yet");
        }

        // Lưu file ảnh và APK
        String previewImageUrl = saveFile(gameRequest.getPreviewImage(), "preview");
        String apkFileUrl = saveFile(gameRequest.getApkFile(), "apk");

        Game game = new Game(
                gameRequest.getName(),
                gameRequest.getDescription(),
                gameRequest.getRequirements(),
                previewImageUrl,
                apkFileUrl,
                gameRequest.isSupportLeaderboard(),
                gameRequest.isSupportPoints(),
                GameStatus.PENDING,
                developer
        );

        try {
            gameRepository.save(game);
            logger.info("Game created successfully: ID={}, Name={}", game.getId(), game.getName());
        } catch (Exception e) {
            logger.error("Error saving game: {}", game.getName(), e);
            throw new GameException("Error saving game");
        }

        // Tạo thông báo API Key
        String apiKeyMessage = generateApiKeyMessage(game);

        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getRequirements(),
                game.getPreviewImageUrl(),
                game.getApkFileUrl(),
                game.isSupportLeaderboard(),
                game.isSupportPoints(),
                game.getStatus(),
                game.getDeveloper().getId(),
                apiKeyMessage
        );
    }

    public List<GameResponse> getGamesByDeveloper(String userEmail) throws GameException {
        logger.info("Fetching games for developer: {}", userEmail);

        User developer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userEmail);
                    return new GameException("User not found");
                });

        if (developer.getRole() != UserRole.DEVELOPER) {
            logger.warn("Game fetch failed - user is not a DEVELOPER: {}", userEmail);
            throw new GameException("Only DEVELOPER accounts can view games");
        }

        List<Game> games = gameRepository.findByDeveloperId(developer.getId());
        return games.stream().map(game -> new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getRequirements(),
                game.getPreviewImageUrl(),
                game.getApkFileUrl(),
                game.isSupportLeaderboard(),
                game.isSupportPoints(),
                game.getStatus(),
                game.getDeveloper().getId(),
                generateApiKeyMessage(game)
        )).collect(Collectors.toList());
    }

    public List<GameResponse> getPublicGames() {
        logger.info("Fetching public approved games");

        List<Game> games = gameRepository.findByStatus(GameStatus.APPROVED);
        return games.stream().map(game -> new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getRequirements(),
                game.getPreviewImageUrl(),
                game.getApkFileUrl(),
                game.isSupportLeaderboard(),
                game.isSupportPoints(),
                game.getStatus(),
                game.getDeveloper().getId(),
                null // Không trả apiKeyMessage cho public
        )).collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file, String prefix) throws GameException {
        if (file == null || file.isEmpty()) {
            logger.warn("File upload failed - no file provided for: {}", prefix);
            throw new GameException("File is required");
        }

        try {
            // Tạo thư mục uploads/ nếu chưa tồn tại
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                logger.info("Created upload directory: {}", UPLOAD_DIR);
            }

            String fileName = prefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + fileName);
            file.transferTo(dest);
            logger.info("File uploaded successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            logger.error("Error uploading file: {}", file.getOriginalFilename(), e);
            throw new GameException("Error uploading file", e);
        }
    }

    private String generateApiKeyMessage(Game game) {
        if (game.getStatus() != GameStatus.APPROVED) {
            return "API will be provided after Admin approves the game.";
        }
        StringBuilder message = new StringBuilder();
        if (game.isSupportLeaderboard()) {
            message.append("Leaderboard API Key: [Simulated_KEY_").append(game.getId()).append("_LB]");
        }
        if (game.isSupportPoints()) {
            if (message.length() > 0) message.append(" | ");
            message.append("Points API Key: [Simulated_KEY_").append(game.getId()).append("_PT]");
        }
        return message.length() > 0 ? message.toString() : "No API Key required.";
    }
}
