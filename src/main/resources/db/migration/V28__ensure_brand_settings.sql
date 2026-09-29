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
)
ON DUPLICATE KEY UPDATE id = 1;
