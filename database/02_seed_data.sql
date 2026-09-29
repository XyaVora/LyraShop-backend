-- Seed/demo records separated from table definitions.
-- Requires 01_schema.sql to have been run first.
SET NAMES utf8mb4;
SET time_zone = '+00:00';
USE lyrashop_db;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- V10__seed_sample_data - records
-- =============================================================================

INSERT IGNORE INTO users (id, email, password, full_name, phone, role, is_active, version, created_at, updated_at) VALUES
(UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'admin@lyrashop.local', '{bcrypt}$2b$12$FxIb3nc1ebB36wD.CbfZLOnIzlgloF8TBAZs6zHEJKJNfoa5.uGAW', 'Quản trị viên Lyra', '0988000001', 'ADMIN', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 'user@lyrashop.local', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Nguyễn Văn An', '0912345678', 'CUSTOMER', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 'hoangmai@gmail.com', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Trần Thị Hoàng Mai', '0987654321', 'CUSTOMER', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 'leminh@gmail.com', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Lê Minh Tuấn', '0909112233', 'CUSTOMER', 1, 0, NOW(), NOW());

INSERT IGNORE INTO categories (id, name, slug, description, parent_id, is_active, created_at, updated_at) VALUES
(1, 'Thời trang nữ', 'thoi-trang-nu', 'Váy đầm, áo kiểu, quần tây và trang phục cao cấp dành cho phái đẹp.', NULL, 1, NOW(), NOW()),
(2, 'Thời trang nam', 'thoi-trang-nam', 'Áo sơ mi, áo polo, vest và phong cách thời trang lịch lãm cho nam giới.', NULL, 1, NOW(), NOW()),
(3, 'Giày dép', 'giay-dep', 'Giày cao gót, giày da Oxford, sneaker và giày lười phong cách.', NULL, 1, NOW(), NOW()),
(4, 'Phụ kiện', 'phu-kien', 'Túi xách da, thắt lưng, khăn lụa và trang sức tinh tế.', NULL, 1, NOW(), NOW());

INSERT IGNORE INTO products (id, category_id, name, slug, description, base_price, is_active, version, created_at, updated_at) VALUES

(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 1, 'Đầm Lụa Satin Cổ V Dáng Dài', 'dam-lua-satin-co-v-dang-dai', 'Chất liệu lụa satin cao cấp bóng nhẹ, phom dáng dài thướt tha tôn vinh nét kiêu sa nữ tính. Thích hợp cho dạ tiệc và sự kiện sang trọng.', 850000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 1, 'Áo Sơ Mi Lụa Tơ Tằm Thêu Tay', 'ao-so-mi-lua-to-tam-theu-tay', 'Chi tiết thêu hoa thủ công tỉ mỉ trên nền lụa tơ tằm mềm mại, thoáng mát và thanh lịch cho quý cô công sở.', 650000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 1, 'Chân Váy Xếp Ly Dáng Xòe Midi', 'chan-vay-xep-ly-dang-xoe-midi', 'Đường xếp ly sắc nét, độ rủ mềm mại giúp từng bước chân thêm uyển chuyển. Dễ dàng phối cùng sơ mi hoặc áo len mỏng.', 520000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 1, 'Áo Blazer Nữ Dáng Suông Công Sở', 'ao-blazer-nu-dang-suong-cong-so', 'Thiết kế vai đệm nhẹ tạo phom chuẩn mực, đường may tinh xảo đem lại diện mạo chuyên nghiệp và cuốn hút.', 1150000.00, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 2, 'Áo Sơ Mi Nam Oxford Trắng Cao Cấp', 'ao-so-mi-nam-oxford-trang-cao-cap', 'Dệt từ 100% sợi bông cotton chải kỹ, bề mặt dệt Oxford đứng phom và ít nhăn, tiêu chuẩn cho phong cách lịch lãm hàng ngày.', 590000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 2, 'Áo Polo Nam Dệt Kim Thoáng Khí', 'ao-polo-nam-det-kim-thoang-khi', 'Cấu trúc dệt kim thông thoáng, thấm hút mồ hôi tốt. Bo cổ dệt tinh tế mang lại vẻ ngoài năng động nhưng lịch thiệp.', 450000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 2, 'Quần Tây Nam Dáng Slimfit Co Giãn', 'quan-tay-nam-dang-slimfit-co-gian', 'Chất vải wool pha co giãn nhẹ, đường ly ép vĩnh viễn giúp tôn dáng đôi chân và tạo sự thoải mái suốt ngày dài.', 680000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 2, 'Áo Khoác Măng Tô Nam Dạ Wool', 'ao-khoac-mang-to-nam-da-wool', 'Dạ ép lông cừu giữ nhiệt vượt trội, phom dài chuẩn phong cách quý ông cổ điển phương Tây.', 1850000.00, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 3, 'Giày Cao Gót Mũi Nhọn Da Bóng 7cm', 'giay-cao-got-mui-nhon-da-bong-7cm', 'Da bóng sang trọng, gót nhọn thanh mảnh với đế đệm êm ái nâng đỡ bàn chân tối ưu khi di chuyển.', 790000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 3, 'Giày Da Oxford Nam Da Bò Ý', 'giay-da-oxford-nam-da-bo-y', 'Da bò nguyên tấm nhập khẩu Ý, đánh xi bóng thủ công patina đẳng cấp dành cho những dịp trang trọng.', 1650000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 3, 'Giày Loafer Da Lộn Phong Cách Ý', 'giay-loafer-da-lon-phong-cach-y', 'Da lộn tự nhiên mềm mịn, phom giày không dây tiện lợi, thoải mái cho những buổi dạo phố cuối tuần.', 950000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 3, 'Giày Sneaker Da Tối Giản Unisex', 'giay-sneaker-da-toi-gian-unisex', 'Thiết kế trắng tinh khôi theo xu hướng tối giản Minimalist, đế cao su nguyên khối bền bỉ và êm nhẹ.', 720000.00, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 4, 'Túi Xách Da Thật Quai Ngọc Trai', 'tui-xach-da-that-quai-ngoc-trai', 'Điểm nhấn quai xách ngọc trai nhân tạo quý phái kết hợp thân túi da bò dập vân tinh tế.', 1450000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 4, 'Thắt Lưng Da Bò Khóa Kim Cổ Điển', 'that-lung-da-bo-khoa-kim-co-dien', 'Mặt khóa kim loại nguyên khối mạ titan chống gỉ, dây da bò 2 lớp bền bỉ theo năm tháng.', 390000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 4, 'Khăn Lụa Vuông Họa Tiết Baroque', 'khan-lua-vuong-hoa-tiet-baroque', 'Họa tiết cổ điển in kỹ thuật số sắc nét trên nền lụa 100%, viền khăn cuốn mép thủ công tỉ mỉ.', 320000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 4, 'Ví Da Cầm Tay Nam Khóa Số', 'vi-da-cam-tay-nam-khoa-so', 'Ví clutch nam tiện dụng với nhiều ngăn đựng điện thoại, thẻ và tiền mặt kèm khóa số an toàn.', 890000.00, 1, 0, NOW(), NOW());

INSERT IGNORE INTO product_variants (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at) VALUES

(UUID_TO_BIN('30000000-0000-0000-0000-000000000101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-S-DEN', 'S', 'Đen', 850000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-M-DEN', 'M', 'Đen', 850000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000103'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-M-BE', 'M', 'Be', 850000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-S-TRANG', 'S', 'Trắng', 650000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-M-TRANG', 'M', 'Trắng', 650000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000203'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-L-HONG', 'L', 'Hồng Pastel', 650000.00, 15, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-S-NAU', 'S', 'Nâu Tây', 520000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-M-DEN', 'M', 'Đen', 520000.00, 45, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-S-KEM', 'S', 'Kem', 1150000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-M-DEN', 'M', 'Đen', 1150000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-M-TRANG', 'M', 'Trắng', 590000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-L-XANH', 'L', 'Xanh Nhạt', 590000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-M-NAVY', 'M', 'Xanh Navy', 450000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-L-XAM', 'L', 'Xám Tiêu', 450000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-30-DEN', '30', 'Đen', 680000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-32-XAM', '32', 'Xám Đậm', 680000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-L-CAMEL', 'L', 'Camel', 1850000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-XL-DEN', 'XL', 'Đen', 1850000.00, 10, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000000901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-36-NUDE', '36', 'Nude', 790000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-37-DEN', '37', 'Đen', 790000.00, 22, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-40-NAU', '40', 'Nâu Cổ Điển', 1650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-41-DEN', '41', 'Đen', 1650000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-40-BO', '40', 'Vàng Bò', 950000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-41-NAVY', '41', 'Navy', 950000.00, 18, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-38-TRANG', '38', 'Trắng', 720000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-41-TRANG', '41', 'Trắng', 720000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 'BAG-PEARL-BE', 'Freesize', 'Be Nhạt', 1450000.00, 15, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-DEN', '115cm', 'Đen', 390000.00, 50, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 'SCARF-SILK-70', '70x70cm', 'Đa sắc', 320000.00, 60, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 'CLUTCH-LOCK-DEN', '28x18cm', 'Đen', 890000.00, 20, 1, 0, NOW(), NOW());

INSERT IGNORE INTO product_images (product_id, variant_id, url, sort_order, is_primary, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), NULL, 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), NULL, 'https://images.unsplash.com/photo-1598554747436-c9293d6a588f?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), NULL, 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), NULL, 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), NULL, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), NULL, 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), NULL, 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), NULL, 'https://images.unsplash.com/photo-1544923246-77307dd654cb?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), NULL, 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), NULL, 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), NULL, 'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), NULL, 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), NULL, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), NULL, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), NULL, 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), NULL, 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=800', 0, 1, NOW());

INSERT IGNORE INTO reviews (product_id, user_id, rating, comment, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Váy mặc rất tôn dáng, lụa mềm mượt và bóng nhẹ rất sang. Giao hàng cực nhanh!', NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Chất vải mát, đường may chuẩn chỉ. Mình mặc dự tiệc cưới ai cũng khen.', NOW() - INTERVAL 2 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Áo thêu tay rất tỉ mỉ, chất tơ tằm mặc nhẹ như không. Rất ưng ý!', NOW() - INTERVAL 4 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Áo sơ mi Oxford phom cực đẹp, vải dày dặn mà không hề bí. Đáng tiền!', NOW() - INTERVAL 3 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Da bò xịn, đi êm chân không bị đau gót. Đóng gói hộp rất cao cấp.', NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 4, 'Túi xinh xắn, quai ngọc trai tạo điểm nhấn rất đẹp. Đựng vừa điện thoại và son phấn.', NOW() - INTERVAL 6 DAY);

INSERT IGNORE INTO orders (id, user_id, total_amount, status, payment_method, payment_status, shipping_address, shipping_phone, note, created_at, updated_at) VALUES
(UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 1440000.00, 'DELIVERED', 'COD', 'PAID', '123 Phố Huế, Phường Bùi Thị Xuân, Quận Hai Bà Trưng, Hà Nội', '0912345678', 'Giao giờ hành chính', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 850000.00, 'SHIPPING', 'COD', 'UNPAID', '45 Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', '0987654321', 'Gọi trước khi giao', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('40000000-0000-0000-0000-000000000003'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 1150000.00, 'PENDING', 'COD', 'UNPAID', '123 Phố Huế, Phường Bùi Thị Xuân, Quận Hai Bà Trưng, Hà Nội', '0912345678', NULL, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR);

INSERT IGNORE INTO order_items (id, order_id, variant_id, product_id, product_name, sku, size, color, quantity, unit_price, subtotal) VALUES
(1, UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'Đầm Lụa Satin Cổ V Dáng Dài', 'DL-SATIN-S-DEN', 'S', 'Đen', 1, 850000.00, 850000.00),
(2, UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'Áo Sơ Mi Nam Oxford Trắng Cao Cấp', 'SM-OXFORD-M-TRANG', 'M', 'Trắng', 1, 590000.00, 590000.00),
(3, UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), UUID_TO_BIN('30000000-0000-0000-0000-000000000102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'Đầm Lụa Satin Cổ V Dáng Dài', 'DL-SATIN-M-DEN', 'M', 'Đen', 1, 850000.00, 850000.00),
(4, UUID_TO_BIN('40000000-0000-0000-0000-000000000003'), UUID_TO_BIN('30000000-0000-0000-0000-000000000401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'Áo Blazer Nữ Dáng Suông Công Sở', 'BZ-NU-S-KEM', 'S', 'Kem', 1, 1150000.00, 1150000.00);

INSERT IGNORE INTO carts (id, user_id, created_at, updated_at) VALUES
(UUID_TO_BIN('50000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), NOW(), NOW());

INSERT IGNORE INTO cart_items (id, cart_id, variant_id, quantity, created_at, updated_at) VALUES
(1, UUID_TO_BIN('50000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000601'), 2, NOW(), NOW());


-- =============================================================================
-- V11__create_wishlist_addresses_promotions - records
-- =============================================================================

INSERT IGNORE INTO promotions
    (id, name, description, discount_percent, start_at, end_at, is_active, created_at, updated_at)
VALUES
    (UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), 'Mùa Sale',
     'Ưu đãi có thời hạn dành cho các thiết kế được chọn.', 20,
     NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, NOW(), NOW());

INSERT IGNORE INTO promotion_products
    (promotion_id, product_id, sale_price, original_price, discount_percent)
SELECT UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), id,
       ROUND(base_price * 0.8, 2), base_price, 20
FROM products
WHERE is_active = 1
ORDER BY created_at, id
LIMIT 8;


-- =============================================================================
-- V12__seed_expanded_catalog_data - records
-- =============================================================================

INSERT IGNORE INTO products (id, category_id, name, slug, description, base_price, is_active, version, created_at, updated_at) VALUES

(UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 1, 'Đầm Dạ Hội Cúp Ngực Phom Dáng Mermaid', 'dam-da-hoi-cup-nguc-mermaid', 'Đầm đuôi cá ôm trọn đường cong cơ thể, cúp ngực đính đá pha lê tinh xảo, chất vải crepe co giãn nhập khẩu cho phong thái đài các tại các đêm tiệc trang trọng.', 1450000.00, 1, 0, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 1, 'Áo Tweed Cropped Dệt Sợi Kim Tuyến', 'ao-tweed-cropped-det-kim-tuyen', 'Áo khoác dạ tweed kinh điển phong cách Pháp, dệt sợi kim tuyến lấp lánh nhẹ, khuy kim loại mạ vàng chạm khắc tỉ mỉ.', 980000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 1, 'Chân Váy Bút Chì Lưng Cao Xẻ Tà', 'chan-vay-but-chi-lung-cao-xe-ta', 'Thiết kế cạp cao tôn vòng eo thon gọn, đường xẻ tà sau duyên dáng giúp bước đi uyển chuyển, chất vải umi cao cấp không nhăn nhàu.', 480000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 1, 'Áo Len Cổ Lọ Cashmere Siêu Nhẹ', 'ao-len-co-lo-cashmere-sieu-nhe', 'Chất len cashmere thượng hạng mềm mịn như mây, giữ ấm hoàn hảo mà vẫn thanh thoát, dễ kết hợp cùng áo blazer hoặc trench coat.', 820000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 1, 'Đầm Suông Linen Thêu Hoa Cổ Tàu', 'dam-suong-linen-theu-hoa-co-tau', 'Vải linen tưng cao cấp đã qua xử lý mềm, phom suông bay bổng thoáng mát đậm chất thơ, họa tiết thêu cành mai thủ công trang nhã.', 750000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 1, 'Quần Tây Nữ Ống Rộng Xếp Ly Đôi', 'quan-tay-nu-ong-rong-xep-ly-doi', 'Xu hướng quần wide-leg thời thượng, cạp cao kèm xếp ly đôi tạo hiệu ứng kéo dài đôi chân tối đa, phong cách tối giản thanh lịch.', 620000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 1, 'Áo Sơ Mi Nữ Tay Phồng Cổ Bèo Victorian', 'ao-so-mi-nu-tay-phong-co-beo-victorian', 'Cảm hứng lãng mạn thời Phục Hưng với cổ xếp bèo mềm mại, tay phồng bo cổ tay tinh tế, chất vải chiffon mỏng nhẹ có lót kín đáo.', 560000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 1, 'Áo Khoác Trench Coat Nữ Dáng Dài Thắt Đai', 'ao-khoac-trench-coat-nu-dang-dai', 'Chiếc áo trench coat kinh điển hai hàng khuy thời trang, chất vải khaki chống thấm nước nhẹ, đai eo thắt tôn dáng chuẩn phong cách London.', 1680000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 1, 'Set Bộ Tweed Áo Khoác Kèm Chân Váy Chữ A', 'set-bo-tweed-ao-khoac-kem-chan-vay', 'Bộ phối hoàn hảo cho quý cô tiểu thư, chất dạ dệt cao cấp phối viền ren sang trọng, dễ tách rời phối đồ linh hoạt.', 1390000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), 1, 'Đầm Maxi Họa Tiết Hoa Nhí Bohemian', 'dam-maxi-hoa-nhi-bohemian', 'Đầm maxi tơ hoa nhí với tầng xòe bồng bềnh, cổ thắt nơ nữ tính, tuyệt vời cho các chuyến du lịch biển và dạo phố mùa hè.', 690000.00, 1, 0, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 2, 'Bộ Suit Nam 2 Mảnh Màu Xanh Navy Đẳng Cấp', 'bo-suit-nam-2-manh-xanh-navy', 'May đo chuẩn phong cách Ý, ve áo xếch quyền lực, chất vải pha len nhập khẩu đứng phom, hoàn hảo cho doanh nhân và sự kiện ngoại giao.', 2650000.00, 1, 0, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 2, 'Áo Sơ Mi Nam Linen Cổ Trụ Phóng Khoáng', 'ao-so-mi-nam-linen-co-tru', '100% sợi linen tự nhiên giặt mềm, cổ tàu hiện đại mang lại cảm giác thư thái, mộc mạc mà sang trọng cho các chuyến đi và ngày hè.', 580000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 2, 'Áo Khoác Da Biker Nam Da Thật Cao Cấp', 'ao-khoac-da-biker-nam-da-that', 'Da cừu mềm mại dẻo dai, khóa kéo kim loại YKK bóng mờ phong cách biker mạnh mẽ, lớp lót dù cách nhiệt êm ái.', 2250000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 2, 'Quần Chinos Nam Co Giãn 4 Chiều', 'quan-chinos-nam-co-gian-4-chieu', 'Vải cotton twill pha spandex đàn hồi tốt, đường may đệm đáy bền bỉ, dễ phối cùng áo thun, polo hoặc sơ mi.', 550000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 2, 'Áo Thun Nam Supima Cotton Tối Giản', 'ao-thun-nam-supima-cotton-toi-gian', 'Dệt từ bông Supima quý hiếm với độ bền gấp đôi sợi cotton thông thường, bề mặt mịn màng không xù lông sau nhiều lần giặt.', 380000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 2, 'Áo Hoodie Nam Nỉ Bông Dày Dặn Streetwear', 'ao-hoodie-nam-ni-bong-streetwear', 'Nỉ bông 380gsm giữ ấm vượt trội, mũ trùm 2 lớp đứng phom, túi kangaroo tiện lợi mang phong cách đường phố năng động.', 650000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 2, 'Áo Len Nam Cổ Tròn Họa Tiết Vặn Thừng Cable Knit', 'ao-len-nam-cable-knit-van-thung', 'Kỹ thuật đan vặn thừng cổ điển phong cách quý tộc Bắc Âu, chất len dệt dày dặn ấm áp, phom suông vừa vặn nam tính.', 720000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 2, 'Áo Khoác Bomber Nam Vải Gió Chống Nước', 'ao-khoac-bomber-nam-vai-gio', 'Vải gió tráng PU ngăn gió và cản nước mưa phùn, bo chun cổ và gấu áo dệt rib co giãn êm, thiết kế trẻ trung hiện đại.', 780000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 2, 'Quần Jean Nam Regular Fit Wash Xanh Vintage', 'quan-jean-nam-regular-fit-vintage', 'Vải denim dệt chéo 13oz bền chắc, xử lý wash rách xước nhẹ tự nhiên tạo điểm nhấn bụi bặm khỏe khoắn.', 680000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 2, 'Áo Polo Nam Phối Cổ Dệt Jacquard', 'ao-polo-nam-phoi-co-jacquard', 'Cổ áo dệt họa tiết hình học Jacquard độc quyền, phom regular fit tôn dáng vai và ngực, mang đến diện mạo chỉn chu cuốn hút.', 490000.00, 1, 0, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 3, 'Giày Chelsea Boot Nam Da Bò Sáp Cổ Điển', 'giay-chelsea-boot-nam-da-bo-sap', 'Da bò sáp Pull-up càng đi càng bóng đẹp theo thời gian, thun co giãn hai bên tiện lợi, đế cao su khâu chỉ kép chắc chắn.', 1450000.00, 1, 0, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 3, 'Giày Sandal Nữ Quai Mảnh Đính Đá Gót Vuông', 'giay-sandal-nu-quai-manh-dinh-da', 'Quai mảnh thanh thoát đính đá lấp lánh, gót vuông cao 5cm vững vàng giúp quý cô tự tin dạo phố hay tham dự sự kiện.', 590000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 3, 'Giày Thể Thao Chunky Sneaker Đế Cao Nữ', 'giay-chunky-sneaker-de-cao-nu', 'Đế đệm bọt khí nâng chiều cao 5cm siêu nhẹ, phối màu pastel năng động, tôn dáng và bảo vệ khớp chân khi hoạt động cả ngày.', 780000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 3, 'Giày Derby Nam Da Bóng Công Sở', 'giay-derby-nam-da-bong-cong-so', 'Kiểu mui hở Derby dễ chịu cho mu bàn chân, da bò đánh bóng gương sang trọng, lót trong êm ái thoáng khí.', 1350000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 3, 'Giày Búp Bê Nữ Mũi Vuông Đính Nơ Da Mềm', 'giay-bup-be-nu-mui-vuong-dinh-no', 'Thiết kế ballet flats mộc mạc, da cừu nhân tạo siêu mềm ôm khít gót không cọ xát, đính nơ kim loại xinh xắn.', 450000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 3, 'Giày Boot Nữ Cổ Thấp Gót Nhọn Da Lì', 'giay-boot-nu-co-thap-got-nhon', 'Ankle boots sành điệu, mũi nhọn sắc sảo kết hợp gót 7cm tôn dáng, khóa kéo hông trơn tru dễ mang tháo.', 890000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 3, 'Giày Lười Nam Penny Loafer Da Bò Mộc', 'giay-luoi-nam-penny-loafer-da-bo', 'Penny Loafer biểu tượng lịch lãm của phong cách Ivy League, viền chỉ may tay thủ công tỉ mỉ, đế cao su chống trượt.', 1150000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 3, 'Giày Mule Sục Nữ Quai Ngang Khóa Vuông', 'giay-mule-suc-nu-quai-ngang', 'Thiết kế sục hở gót tiện lợi thời thượng, mặt khóa vuông mạ vàng làm điểm nhấn, dễ phối cùng quần tây hoặc váy midi.', 520000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 3, 'Giày Sneaker Nam Cổ Thấp Phối Da Lộn Thể Thao', 'giay-sneaker-nam-co-thap-da-lon', 'Phối hợp da trơn và da lộn tương phản tinh tế, đế cao su lưu hóa đàn hồi giảm xóc tốt khi chạy bộ và vận động.', 850000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 3, 'Dép Quai Ngang Nam Da Bò Đế Trấu Công Thái Học', 'dep-quai-ngang-nam-da-bo-de-trau', 'Đế trấu tự nhiên uốn lượn theo vòm bàn chân nâng đỡ cột sống, quai da bò thật có khóa kim loại điều chỉnh độ rộng.', 490000.00, 1, 0, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),


(UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), 4, 'Đồng Hồ Nam Dây Da Tối Giản Chronograph', 'dong-ho-nam-day-da-chronograph', 'Mặt kính sapphire chống xước tuyệt đối, vỏ thép không gỉ 316L mạ PVD, máy quartz Nhật Bản chính xác kèm tính năng bấm giờ.', 1850000.00, 1, 0, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), 4, 'Đồng Hồ Nữ Dây Kim Loại Đính Đá Pha Lê', 'dong-ho-nu-day-kim-loai-dinh-da', 'Mặt khảm xà cừ thiên nhiên lấp lánh đổi màu theo góc sáng, viền đính đá pha lê Swarovski kiêu sa, dây kim loại mắt lưới nhuyễn.', 1650000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 4, 'Kính Mát Unisex Mắt Vuông Gọng Acetate Polarized', 'kinh-mat-unisex-gong-acetate-polarized', 'Tròng kính phân cực Polarized chống tia UV400 bảo vệ mắt tối ưu, gọng nhựa acetate bóng bẩy phong cách unisex cá tính.', 680000.00, 1, 0, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 4, 'Túi Tote Nữ Da Thật Cỡ Lớn Đựng Vừa Laptop', 'tui-tote-nu-da-that-co-lon', 'Da bò hạt mềm mại bền bỉ, không gian rộng rãi chứa thoải mái tài liệu và laptop 14 inch, phong cách thanh lịch cho nữ văn phòng.', 1550000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000051'), 4, 'Balo Da Nam Đa Năng Chống Nước Công Sở', 'balo-da-nam-da-nang-chong-nuoc', 'Da nhân tạo phủ bóng chống thấm cao cấp, ngăn laptop chống sốc chuyên dụng, quai đeo êm ái trợ lực cho chàng trai bận rộn.', 1120000.00, 1, 0, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), 4, 'Cà Vạt Lụa Tơ Tằm Nam Kèm Kẹp Cà Vạt Mạ Vàng', 'ca-vat-lua-to-tam-nam-kem-kep', '100% lụa dệt hoa văn chìm tinh tế, bề rộng 7cm chuẩn phong cách hiện đại, tặng kèm kẹp cà vạt đồng bộ sang trọng.', 420000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000053'), 4, 'Khuyên Tai Bạc Nữ Ý 925 Đính Ngọc Trai Nuôi', 'khuyen-tai-bac-nu-dinh-ngoc-trai', 'Bạc 925 mạ bạch kim chống xỉn màu, viên ngọc trai nước ngọt sáng bóng tròn đều tạo nét đẹp dịu dàng quý phái.', 390000.00, 1, 0, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000054'), 4, 'Vòng Tay Da Nam Khóa Thép Titan Không Gỉ', 'vong-tay-da-nam-khoa-thep-titan', 'Sợi da bện tròn thủ công chắc chắn, đầu khóa nam châm bằng thép titan chống gỉ khắc logo tinh xảo, phong cách phóng khoáng.', 320000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), 4, 'Mũ Nồi Beret Nữ Dạ Len Cổ Điển Kiểu Pháp', 'mu-noi-beret-nu-da-len-kieu-phap', 'Chất dạ len ép 100% giữ phom tròn đầy, mang lại vẻ ngoài lãng mạn như quý cô Paris vào mùa thu đông.', 290000.00, 1, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 4, 'Ví Nam Mini Da Bò Sáp Chống Trộm Sóng RFID', 'vi-nam-mini-da-bo-chong-trom-rfid', 'Kích thước bỏ túi áo nhỏ gọn, tích hợp lớp lót kim loại chống quét trộm thẻ tín dụng RFID, da bò sáp bụi bặm cá tính.', 360000.00, 1, 0, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY);

INSERT IGNORE INTO product_variants (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at) VALUES

(UUID_TO_BIN('30000000-0000-0000-0000-000000001701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-S-DEN', 'S', 'Đen Huyền Bí', 1450000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-M-DEN', 'M', 'Đen Huyền Bí', 1450000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-M-DO', 'M', 'Đỏ Rượu Vang', 1450000.00, 18, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-S-KEM', 'S', 'Trắng Kem', 980000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-M-KEM', 'M', 'Trắng Kem', 980000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001803'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-M-XANH', 'M', 'Xanh Pastel', 980000.00, 22, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000001901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-S-DEN', 'S', 'Đen', 480000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-M-DEN', 'M', 'Đen', 480000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001903'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-M-NAU', 'M', 'Nâu Camel', 480000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-S-BE', 'S', 'Be Yến Mạch', 820000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-M-BE', 'M', 'Be Yến Mạch', 820000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002003'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-L-NAU', 'L', 'Nâu Mocha', 820000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-M-TRANG', 'M', 'Trắng Tinh', 750000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-L-XANH', 'L', 'Xanh Cốm', 750000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-S-XAM', 'S', 'Xám Khói', 620000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-M-DEN', 'M', 'Đen', 620000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-S-TRANG', 'S', 'Trắng Kem', 560000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-M-HONG', 'M', 'Hồng Nude', 560000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-S-BE', 'S', 'Be Cổ Điển', 1680000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-M-DEN', 'M', 'Đen', 1680000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-S-DO', 'S', 'Đỏ Burgundy', 1390000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-M-XANH', 'M', 'Xanh Baby', 1390000.00, 22, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), 'MAXI-BOHO-F-VANG', 'Freesize', 'Vàng Mù Tạt', 690000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), 'MAXI-BOHO-F-XANH', 'Freesize', 'Xanh Mint', 690000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-48-NAVY', '48 (M)', 'Xanh Navy', 2650000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-50-NAVY', '50 (L)', 'Xanh Navy', 2650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-52-NAVY', '52 (XL)', 'Xanh Navy', 2650000.00, 10, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-M-TRANG', 'M', 'Trắng', 580000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-L-DENIM', 'L', 'Xanh Denim', 580000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000002901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-M-DEN', 'M', 'Đen', 2250000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-L-NAU', 'L', 'Nâu Socola', 2250000.00, 12, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-30-BE', '30', 'Be Khaki', 550000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-32-REU', '32', 'Xanh Rêu', 550000.00, 30, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-M-TRANG', 'M', 'Trắng', 380000.00, 60, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-L-DEN', 'L', 'Đen', 380000.00, 50, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-M-XAM', 'M', 'Xám Tiêu', 650000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-L-DEN', 'L', 'Đen', 650000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-M-KEM', 'M', 'Kem', 720000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-L-THAN', 'L', 'Xanh Than', 720000.00, 30, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-M-REU', 'M', 'Xanh Rêu', 780000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-L-DEN', 'L', 'Đen', 780000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-30-XANH', '30', 'Xanh Nhạt', 680000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-32-CHAM', '32', 'Xanh Chàm', 680000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-M-TRANG', 'M', 'Trắng Phối Xanh', 490000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-L-DEN', 'L', 'Đen Phối Vàng', 490000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-40-NAU', '40', 'Nâu Sáp', 1450000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-41-NAU', '41', 'Nâu Sáp', 1450000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-42-DEN', '42', 'Đen', 1450000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-38-BAC', '36', 'Ánh Bạc', 590000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-37-BAC', '37', 'Ánh Bạc', 590000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003803'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-38-DEN', '38', 'Đen', 590000.00, 22, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000003901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-36-TRANG', '36', 'Trắng Phối Hồng', 780000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-37-TRANG', '37', 'Trắng Phối Hồng', 780000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-40-DEN', '40', 'Đen', 1350000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-41-DEN', '41', 'Đen', 1350000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-36-BE', '36', 'Be', 450000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-37-BE', '37', 'Be', 450000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-36-DEN', '36', 'Đen', 890000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-37-DEN', '37', 'Đen', 890000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-40-NAU', '40', 'Nâu Hạt Dẻ', 1150000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-41-NAU', '41', 'Nâu Hạt Dẻ', 1150000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-36-KEM', '36', 'Trắng Kem', 520000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-37-KEM', '37', 'Trắng Kem', 520000.00, 30, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-40-XAM', '40', 'Xám Khói', 850000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-41-XAM', '41', 'Xám Khói', 850000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-40-NAU', '40', 'Nâu Đất', 490000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-41-NAU', '41', 'Nâu Đất', 490000.00, 35, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), 'DH-CHRONO-40-NAU', 'Mặt 40mm', 'Dây Nâu Mặt Trắng', 1850000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), 'DH-CHRONO-40-DEN', 'Mặt 40mm', 'Dây Đen Mặt Đen', 1850000.00, 20, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), 'DH-CRYSTAL-28-VANG', 'Mặt 28mm', 'Vàng Hồng', 1650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), 'DH-CRYSTAL-28-BAC', 'Mặt 28mm', 'Ánh Bạc', 1650000.00, 18, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000004901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 'KM-POLAR-FREE-DEN', 'Freesize', 'Đen Bóng', 680000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 'KM-POLAR-FREE-NAU', 'Freesize', 'Đồi Mồi Nâu', 680000.00, 30, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 'TOTE-LEATHER-38-DEN', '38x30cm', 'Đen', 1550000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 'TOTE-LEATHER-38-NAU', '38x30cm', 'Nâu Bò', 1550000.00, 25, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000051'), 'BP-WATER-42-DEN', '42x30cm', 'Đen Mờ', 1120000.00, 30, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), 'CV-SILK-7-NAVY', '7x145cm', 'Xanh Navy Họa Tiết', 420000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), 'CV-SILK-7-DO', '7x145cm', 'Đỏ Rượu Chìm', 420000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000053'), 'KT-PEARL-8-BAC', '8mm', 'Bạc Ánh Kim', 390000.00, 60, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000054'), 'VT-TITAN-20-DEN', '20cm', 'Đen', 320000.00, 45, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), 'MU-BERET-FREE-DEN', 'Freesize', 'Đen', 290000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), 'MU-BERET-FREE-BE', 'Freesize', 'Be Kem', 290000.00, 40, 1, 0, NOW(), NOW()),


(UUID_TO_BIN('30000000-0000-0000-0000-000000005601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 'VI-RFID-10-NAU', '10x8cm', 'Nâu Cà Phê', 360000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 'VI-RFID-10-DEN', '10x8cm', 'Đen', 360000.00, 40, 1, 0, NOW(), NOW());

INSERT IGNORE INTO product_images (product_id, variant_id, url, sort_order, is_primary, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), NULL, 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), NULL, 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), NULL, 'https://images.unsplash.com/photo-1582142306909-195724d33ffc?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), NULL, 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), NULL, 'https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), NULL, 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), NULL, 'https://images.unsplash.com/photo-1604014237800-1c9102c219da?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), NULL, 'https://images.unsplash.com/photo-1544441893-675973e31985?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), NULL, 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), NULL, 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800', 0, 1, NOW()),

(UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), NULL, 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), NULL, 'https://images.unsplash.com/photo-1602810316693-3667c854239a?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), NULL, 'https://images.unsplash.com/photo-1520975954732-35dd22299614?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), NULL, 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), NULL, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), NULL, 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), NULL, 'https://images.unsplash.com/photo-1614495039157-1e5b871c828d?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), NULL, 'https://images.unsplash.com/photo-1548883354-7622d03aca27?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), NULL, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), NULL, 'https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=800', 0, 1, NOW()),

(UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), NULL, 'https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), NULL, 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), NULL, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), NULL, 'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), NULL, 'https://images.unsplash.com/photo-1562273138-f46be4ebdf33?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), NULL, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), NULL, 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), NULL, 'https://images.unsplash.com/photo-1535043934128-cf0b28d52f95?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), NULL, 'https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), NULL, 'https://images.unsplash.com/photo-1603808033192-082d6919d3e1?w=800', 0, 1, NOW()),

(UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), NULL, 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), NULL, 'https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), NULL, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), NULL, 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000051'), NULL, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), NULL, 'https://images.unsplash.com/photo-1589756823695-278bc923f962?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000053'), NULL, 'https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000054'), NULL, 'https://images.unsplash.com/photo-1611591475822-79017f8a70a8?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), NULL, 'https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), NULL, 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=800', 0, 1, NOW());

INSERT IGNORE INTO reviews (product_id, user_id, rating, comment, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Váy ôm dáng cực chuẩn, đính đá sáng lấp lánh mà không hề sến. Rất đáng đồng tiền!', NOW() - INTERVAL 4 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Áo tweed xinh xỉu, mặc vào trông tiểu thư sang chảnh liền. Khuy áo rất chắc chắn.', NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Chất len cashmere siêu mềm, không hề ngứa hay ráp da. Giữ ấm rất tốt.', NOW() - INTERVAL 3 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Quần ống rộng hack dáng đỉnh cao, vải rủ đẹp không nhăn khi ngồi lâu.', NOW() - INTERVAL 2 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Trench coat form dáng London cực ngầu, đường kim mũi chỉ nét căng.', NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Bộ suit may rất khéo, đệm vai vừa vặn, màu xanh navy lên hình rất quyền lực.', NOW() - INTERVAL 6 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 4, 'Vải linen thoáng mát, đi biển hay cafe cuối tuần đều rất hợp vibe.', NOW() - INTERVAL 4 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Da cừu thật mềm và thơm mùi da tự nhiên, form biker mặc vào rất nam tính.', NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Cotton Supima xịn thật sự, giặt máy mấy lần vẫn không nhão cổ hay xù lông.', NOW() - INTERVAL 3 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Chelsea boot chất da sáp rất bụi, đi êm chân không bị cứng mu.', NOW() - INTERVAL 2 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Sandal xinh lung linh, gót vuông đi vững vàng không bị mỏi chân.', NOW() - INTERVAL 3 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Đế cao su êm ái, nâng chiều cao tự nhiên mà giày lại nhẹ tênh.', NOW() - INTERVAL 4 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Đồng hồ mặt kính sapphire trong veo, dây da mềm đeo ôm tay rất sang.', NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Mặt xà cừ đổi màu dưới nắng siêu đẹp, đóng gói hộp nhung làm quà tặng rất xịn.', NOW() - INTERVAL 2 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Túi tote to đựng vừa macbook 14inch và sổ tay, da mềm quai xách êm.', NOW() - INTERVAL 3 DAY);

INSERT IGNORE INTO promotion_products (promotion_id, product_id, sale_price, original_price, discount_percent) VALUES
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 784000.00, 980000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 600000.00, 750000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 464000.00, 580000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 440000.00, 550000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 1160000.00, 1450000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 624000.00, 780000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 544000.00, 680000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 288000.00, 360000.00, 20);


-- =============================================================================
-- V13__complete_storefront_features - records
-- =============================================================================

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount = 0;


-- =============================================================================
-- V14__snapshot_product_id_on_order_items - records
-- =============================================================================

UPDATE order_items item
JOIN product_variants variant ON variant.id = item.variant_id
SET item.product_id = variant.product_id;


-- =============================================================================
-- V16__create_vouchers - records
-- =============================================================================

INSERT IGNORE INTO vouchers (id, code, label, voucher_type, discount_type, discount_value,
    max_discount_amount, minimum_order_amount, starts_at, ends_at, total_usage_limit, per_user_limit)
VALUES
    (UNHEX('10000000000000000000000000000001'), 'LYRA10', 'Giảm 10% giá trị sản phẩm',
     'discount', 'PERCENT', 10, NULL, 0, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20),
    (UNHEX('10000000000000000000000000000002'), 'FREESHIP', 'Miễn phí vận chuyển',
     'shipping', 'FREESHIP', 0, NULL, 0, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20),
    (UNHEX('10000000000000000000000000000003'), 'LYRA50K', 'Giảm 50.000đ cho đơn từ 800.000đ',
     'discount', 'FIXED', 50000, NULL, 800000, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20);


-- =============================================================================
-- V21__secure_newsletter_consent - records
-- =============================================================================

UPDATE newsletter_subscriptions SET confirmed_at = created_at WHERE is_active = TRUE;


-- =============================================================================
-- V22__add_order_expiration - records
-- =============================================================================

UPDATE orders
SET expires_at = CASE
    WHEN payment_method = 'VNPAY' AND payment_status = 'UNPAID'
        THEN DATE_ADD(created_at, INTERVAL 30 MINUTE)
    ELSE DATE_ADD(created_at, INTERVAL 24 HOUR)
END
WHERE status = 'PENDING';


-- =============================================================================
-- V24__create_brand_settings - records
-- =============================================================================

INSERT IGNORE INTO brand_settings (id, name, tagline, story, founded, hotline, email, address)
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


-- =============================================================================
-- V25__expire_newsletter_confirmation_tokens - records
-- =============================================================================

UPDATE newsletter_subscriptions
SET confirmation_expires_at = DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 24 HOUR)
WHERE confirmation_token_hash IS NOT NULL;


-- =============================================================================
-- V28__ensure_brand_settings - records
-- =============================================================================

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


-- =============================================================================
-- V31__expand_sample_product_variants - records
-- =============================================================================

INSERT IGNORE INTO product_variants
    (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at)
VALUES
(UUID_TO_BIN('31000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-L-DEN', 'L', 'Đen', 850000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-S-BE', 'S', 'Be', 850000.00, 36, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000003'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-L-BE', 'L', 'Be', 850000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000004'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-S-BURG', 'S', 'Đỏ Burgundy', 850000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000005'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-M-BURG', 'M', 'Đỏ Burgundy', 850000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000006'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-L-BURG', 'L', 'Đỏ Burgundy', 850000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000007'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-S-HONG', 'S', 'Hồng Pastel', 650000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000008'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-M-HONG', 'M', 'Hồng Pastel', 650000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000009'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-L-TRANG', 'L', 'Trắng', 650000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000010'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-XL-TRANG', 'XL', 'Trắng', 650000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000011'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-XL-HONG', 'XL', 'Hồng Pastel', 650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000012'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-S-DEN', 'S', 'Đen', 520000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000013'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-M-NAU', 'M', 'Nâu Tây', 520000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000014'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-L-NAU', 'L', 'Nâu Tây', 520000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000015'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-L-DEN', 'L', 'Đen', 520000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000016'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-S-DEN', 'S', 'Đen', 1150000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000017'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-M-KEM', 'M', 'Kem', 1150000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000018'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-L-KEM', 'L', 'Kem', 1150000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000019'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-L-DEN', 'L', 'Đen', 1150000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000020'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-XL-KEM', 'XL', 'Kem', 1150000.00, 26, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000021'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-XL-DEN', 'XL', 'Đen', 1150000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000022'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-M-XANH', 'M', 'Xanh Nhạt', 590000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000023'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-L-TRANG', 'L', 'Trắng', 590000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000024'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-XL-TRANG', 'XL', 'Trắng', 590000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000025'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-XL-XANH', 'XL', 'Xanh Nhạt', 590000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000026'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-M-XAM', 'M', 'Xám Tiêu', 450000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000027'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-L-NAVY', 'L', 'Xanh Navy', 450000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000028'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-XL-NAVY', 'XL', 'Xanh Navy', 450000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000029'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-XL-XAM', 'XL', 'Xám Tiêu', 450000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000030'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-30-XAM', '30', 'Xám Đậm', 680000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000031'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-32-DEN', '32', 'Đen', 680000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000032'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-34-DEN', '34', 'Đen', 680000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000033'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-34-XAM', '34', 'Xám Đậm', 680000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000034'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-M-CAMEL', 'M', 'Camel', 1850000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000035'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-M-DEN', 'M', 'Đen', 1850000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000036'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-L-DEN', 'L', 'Đen', 1850000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000037'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-XL-CAMEL', 'XL', 'Camel', 1850000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000038'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-35-NUDE', '35', 'Nude', 790000.00, 22, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000039'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-35-DEN', '35', 'Đen', 790000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000040'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-36-DEN', '36', 'Đen', 790000.00, 36, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000041'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-37-NUDE', '37', 'Nude', 790000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000042'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-38-NUDE', '38', 'Nude', 790000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000043'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-38-DEN', '38', 'Đen', 790000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000044'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-39-NAU', '39', 'Nâu Cổ Điển', 1650000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000045'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-39-DEN', '39', 'Đen', 1650000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000046'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-40-DEN', '40', 'Đen', 1650000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000047'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-41-NAU', '41', 'Nâu Cổ Điển', 1650000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000048'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-42-NAU', '42', 'Nâu Cổ Điển', 1650000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000049'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-42-DEN', '42', 'Đen', 1650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000050'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-39-BO', '39', 'Vàng Bò', 950000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000051'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-39-NAVY', '39', 'Navy', 950000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000052'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-40-NAVY', '40', 'Navy', 950000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000053'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-41-BO', '41', 'Vàng Bò', 950000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000054'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-42-BO', '42', 'Vàng Bò', 950000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000055'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-42-NAVY', '42', 'Navy', 950000.00, 31, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000056'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-37-TRANG', '37', 'Trắng', 720000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000057'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-39-TRANG', '39', 'Trắng', 720000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000058'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-40-TRANG', '40', 'Trắng', 720000.00, 26, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000059'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-42-TRANG', '42', 'Trắng', 720000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000060'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-43-TRANG', '43', 'Trắng', 720000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000061'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-38-DEN', '38', 'Đen', 720000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000062'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-39-DEN', '39', 'Đen', 720000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000063'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-40-DEN', '40', 'Đen', 720000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000064'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-41-DEN', '41', 'Đen', 720000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000065'), UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 'BAG-PEARL-DEN', 'Freesize', 'Đen', 1450000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000066'), UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 'BAG-PEARL-BURG', 'Freesize', 'Đỏ Burgundy', 1450000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000067'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-105-DEN', '105cm', 'Đen', 390000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000068'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-105-NAU', '105cm', 'Nâu', 390000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000069'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-115-NAU', '115cm', 'Nâu', 390000.00, 22, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000070'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-125-DEN', '125cm', 'Đen', 390000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000071'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-125-NAU', '125cm', 'Nâu', 390000.00, 36, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000072'), UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 'SCARF-SILK-70-NAVY', '70x70cm', 'Xanh Navy', 320000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000073'), UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 'SCARF-SILK-70-BURG', '70x70cm', 'Đỏ Burgundy', 320000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000074'), UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 'CLUTCH-LOCK-NAU', '28x18cm', 'Nâu', 890000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('31000000-0000-0000-0000-000000000075'), UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 'CLUTCH-LOCK-NAVY', '28x18cm', 'Xanh Navy', 890000.00, 12, 1, 0, NOW(), NOW());


-- =============================================================================
-- V32__expand_remaining_product_variants - records
-- =============================================================================

UPDATE product_variants
SET sku = 'SD-MANH-36-BAC'
WHERE sku = 'SD-MANH-38-BAC' AND size = '36';

INSERT IGNORE INTO product_variants
    (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at)
VALUES
(UUID_TO_BIN('32000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-S-DO', 'S', 'Đỏ Rượu Vang', 1450000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-L-DEN', 'L', 'Đen Huyền Bí', 1450000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000003'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-L-DO', 'L', 'Đỏ Rượu Vang', 1450000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000004'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-S-XANH', 'S', 'Xanh Pastel', 980000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000005'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-L-KEM', 'L', 'Trắng Kem', 980000.00, 11, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000006'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-L-XANH', 'L', 'Xanh Pastel', 980000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000007'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-S-NAU', 'S', 'Nâu Camel', 480000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000008'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-L-DEN', 'L', 'Đen', 480000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000009'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-L-NAU', 'L', 'Nâu Camel', 480000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000010'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-S-NAU', 'S', 'Nâu Mocha', 820000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000011'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-M-NAU', 'M', 'Nâu Mocha', 820000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000012'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-L-BE', 'L', 'Be Yến Mạch', 820000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000013'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-M-XANH', 'M', 'Xanh Cốm', 750000.00, 10, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000014'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-L-TRANG', 'L', 'Trắng Tinh', 750000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000015'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-S-TRANG', 'S', 'Trắng Tinh', 750000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000016'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-S-XANH', 'S', 'Xanh Cốm', 750000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000017'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-S-DEN', 'S', 'Đen', 620000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000018'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-M-XAM', 'M', 'Xám Khói', 620000.00, 11, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000019'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-L-DEN', 'L', 'Đen', 620000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000020'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-L-XAM', 'L', 'Xám Khói', 620000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000021'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-S-HONG', 'S', 'Hồng Nude', 560000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000022'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-M-TRANG', 'M', 'Trắng Kem', 560000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000023'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-L-TRANG', 'L', 'Trắng Kem', 560000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000024'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-L-HONG', 'L', 'Hồng Nude', 560000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000025'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-S-DEN', 'S', 'Đen', 1680000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000026'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-M-BE', 'M', 'Be Cổ Điển', 1680000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000027'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-L-BE', 'L', 'Be Cổ Điển', 1680000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000028'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-L-DEN', 'L', 'Đen', 1680000.00, 13, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000029'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-S-XANH', 'S', 'Xanh Baby', 1390000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000030'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-M-DO', 'M', 'Đỏ Burgundy', 1390000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000031'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-L-DO', 'L', 'Đỏ Burgundy', 1390000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000032'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-L-XANH', 'L', 'Xanh Baby', 1390000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000033'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-48-XAM', '48 (M)', 'Xám Than', 2650000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000034'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-50-XAM', '50 (L)', 'Xám Than', 2650000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000035'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-52-XAM', '52 (XL)', 'Xám Than', 2650000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000036'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-M-DENIM', 'M', 'Xanh Denim', 580000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000037'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-L-TRANG', 'L', 'Trắng', 580000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000038'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-XL-TRANG', 'XL', 'Trắng', 580000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000039'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-XL-DENIM', 'XL', 'Xanh Denim', 580000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000040'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-M-NAU', 'M', 'Nâu Socola', 2250000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000041'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-L-DEN', 'L', 'Đen', 2250000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000042'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-XL-DEN', 'XL', 'Đen', 2250000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000043'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-XL-NAU', 'XL', 'Nâu Socola', 2250000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000044'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-30-REU', '30', 'Xanh Rêu', 550000.00, 11, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000045'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-32-BE', '32', 'Be Khaki', 550000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000046'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-34-BE', '34', 'Be Khaki', 550000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000047'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-34-REU', '34', 'Xanh Rêu', 550000.00, 26, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000048'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-M-DEN', 'M', 'Đen', 380000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000049'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-L-TRANG', 'L', 'Trắng', 380000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000050'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-XL-TRANG', 'XL', 'Trắng', 380000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000051'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-XL-DEN', 'XL', 'Đen', 380000.00, 22, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000052'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-M-DEN', 'M', 'Đen', 650000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000053'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-L-XAM', 'L', 'Xám Tiêu', 650000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000054'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-XL-DEN', 'XL', 'Đen', 650000.00, 13, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000055'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-XL-XAM', 'XL', 'Xám Tiêu', 650000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000056'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-M-THAN', 'M', 'Xanh Than', 720000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000057'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-L-KEM', 'L', 'Kem', 720000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000058'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-XL-KEM', 'XL', 'Kem', 720000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000059'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-XL-THAN', 'XL', 'Xanh Than', 720000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000060'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-M-DEN', 'M', 'Đen', 780000.00, 26, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000061'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-L-REU', 'L', 'Xanh Rêu', 780000.00, 31, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000062'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-XL-DEN', 'XL', 'Đen', 780000.00, 36, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000063'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-XL-REU', 'XL', 'Xanh Rêu', 780000.00, 10, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000064'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-30-CHAM', '30', 'Xanh Chàm', 680000.00, 22, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000065'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-32-XANH', '32', 'Xanh Nhạt', 680000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000066'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-34-CHAM', '34', 'Xanh Chàm', 680000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000067'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-34-XANH', '34', 'Xanh Nhạt', 680000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000068'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-M-DEN', 'M', 'Đen Phối Vàng', 490000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000069'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-L-TRANG', 'L', 'Trắng Phối Xanh', 490000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000070'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-XL-DEN', 'XL', 'Đen Phối Vàng', 490000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000071'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-XL-TRANG', 'XL', 'Trắng Phối Xanh', 490000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000072'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-40-DEN', '40', 'Đen', 1450000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000073'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-41-DEN', '41', 'Đen', 1450000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000074'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-42-NAU', '42', 'Nâu Sáp', 1450000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000075'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-43-NAU', '43', 'Nâu Sáp', 1450000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000076'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-43-DEN', '43', 'Đen', 1450000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000077'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-36-DEN', '36', 'Đen', 590000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000078'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-37-DEN', '37', 'Đen', 590000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000079'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-38-BAC', '38', 'Ánh Bạc', 590000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000080'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-39-BAC', '39', 'Ánh Bạc', 590000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000081'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-39-DEN', '39', 'Đen', 590000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000082'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-36-DEN', '36', 'Đen', 780000.00, 16, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000083'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-37-DEN', '37', 'Đen', 780000.00, 21, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000084'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-38-TRANG', '38', 'Trắng Phối Hồng', 780000.00, 26, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000085'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-38-DEN', '38', 'Đen', 780000.00, 31, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000086'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-40-NAU', '40', 'Nâu Cổ Điển', 1350000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000087'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-41-NAU', '41', 'Nâu Cổ Điển', 1350000.00, 17, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000088'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-42-DEN', '42', 'Đen', 1350000.00, 22, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000089'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-42-NAU', '42', 'Nâu Cổ Điển', 1350000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000090'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-36-DEN', '36', 'Đen', 450000.00, 39, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000091'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-37-DEN', '37', 'Đen', 450000.00, 13, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000092'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-38-BE', '38', 'Be', 450000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000093'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-38-DEN', '38', 'Đen', 450000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000094'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-36-NAU', '36', 'Nâu Camel', 890000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000095'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-37-NAU', '37', 'Nâu Camel', 890000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000096'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-38-DEN', '38', 'Đen', 890000.00, 14, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000097'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-38-NAU', '38', 'Nâu Camel', 890000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000098'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-40-DEN', '40', 'Đen', 1150000.00, 31, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000099'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-41-DEN', '41', 'Đen', 1150000.00, 36, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000100'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-42-NAU', '42', 'Nâu Hạt Dẻ', 1150000.00, 10, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-42-DEN', '42', 'Đen', 1150000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-36-DEN', '36', 'Đen', 520000.00, 27, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000103'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-37-DEN', '37', 'Đen', 520000.00, 32, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000104'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-38-KEM', '38', 'Trắng Kem', 520000.00, 37, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000105'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-38-DEN', '38', 'Đen', 520000.00, 11, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000106'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-40-NAVY', '40', 'Xanh Navy', 850000.00, 23, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000107'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-41-NAVY', '41', 'Xanh Navy', 850000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000108'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-42-XAM', '42', 'Xám Khói', 850000.00, 33, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000109'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-42-NAVY', '42', 'Xanh Navy', 850000.00, 38, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000110'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-40-DEN', '40', 'Đen', 490000.00, 19, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000111'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-41-DEN', '41', 'Đen', 490000.00, 24, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000112'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-42-NAU', '42', 'Nâu Đất', 490000.00, 29, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000113'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-42-DEN', '42', 'Đen', 490000.00, 34, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000114'), UUID_TO_BIN('20000000-0000-0000-0000-000000000051'), 'BP-WATER-42-NAU', '42x30cm', 'Nâu Đậm', 1120000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000115'), UUID_TO_BIN('20000000-0000-0000-0000-000000000053'), 'KT-PEARL-8-VANG', '8mm', 'Vàng Hồng', 390000.00, 31, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('32000000-0000-0000-0000-000000000116'), UUID_TO_BIN('20000000-0000-0000-0000-000000000054'), 'VT-TITAN-20-NAU', '20cm', 'Nâu', 320000.00, 12, 1, 0, NOW(), NOW());


-- =============================================================================
-- Final-schema data normalization
-- =============================================================================

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount = 0;

UPDATE orders
SET expires_at = CASE
    WHEN payment_method = 'VNPAY' AND payment_status = 'UNPAID'
        THEN DATE_ADD(created_at, INTERVAL 30 MINUTE)
    ELSE DATE_ADD(created_at, INTERVAL 24 HOUR)
END
WHERE status = 'PENDING' AND expires_at IS NULL;

SET FOREIGN_KEY_CHECKS = 1;
