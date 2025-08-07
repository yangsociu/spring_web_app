// Note: Tạo bảng users trong MySQL để lưu thông tin người dùng.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,

    //information
    full_name VARCHAR(100),
    portfolio_url VARCHAR(255),
    experience_years INT,
    status VARCHAR(50) NOT NULL
);

// Note: Tạo bảng games trong MySQL để lưu thông tin game do Developer upload.
CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    preview_image_url VARCHAR(255),
    apk_file_url VARCHAR(255),
    support_leaderboard BOOLEAN NOT NULL DEFAULT FALSE,
    support_points BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    developer_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (developer_id) REFERENCES users(id)
);

