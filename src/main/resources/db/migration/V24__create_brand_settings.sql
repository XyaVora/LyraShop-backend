CREATE TABLE brand_settings (
    id SMALLINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    story TEXT NOT NULL,
    founded INT NOT NULL,
    hotline VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_brand_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_brand_settings_founded CHECK (founded BETWEEN 1800 AND 9999)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT INTO brand_settings (id, name, tagline, story, founded, hotline, email, address)
VALUES (
    1,
    'LYRA',
    'Phong cách định nghĩa bạn',
    'Từ xưởng may thủ công, LYRA theo đuổi sự hoàn hảo trong từng đường kim mũi chỉ. Mỗi bộ sưu tập kết hợp phom dáng hiện đại với chất liệu thiên nhiên thân thiện với môi trường.',
    2018,
    '1900 1234',
    'hello@lyra.vn',
    '128 Phố Huế, Hai Bà Trưng, Hà Nội'
);
