-- =============================================================================
--  V12 — Seed Expanded Fashion Catalog (Men, Women, Footwear & Accessories)
-- =============================================================================

-- 1. PRODUCTS
INSERT IGNORE INTO products (id, category_id, name, slug, description, base_price, is_active, version, created_at, updated_at) VALUES
-- Category 1: Thời trang nữ
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

-- Category 2: Thời trang nam
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

-- Category 3: Giày dép
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

-- Category 4: Phụ kiện
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

-- 2. PRODUCT VARIANTS
INSERT IGNORE INTO product_variants (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at) VALUES
-- SP 17 (Đầm dạ hội mermaid)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-S-DEN', 'S', 'Đen Huyền Bí', 1450000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-M-DEN', 'M', 'Đen Huyền Bí', 1450000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000017'), 'DDH-MERMAID-M-DO', 'M', 'Đỏ Rượu Vang', 1450000.00, 18, 1, 0, NOW(), NOW()),

-- SP 18 (Áo tweed cropped)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-S-KEM', 'S', 'Trắng Kem', 980000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-M-KEM', 'M', 'Trắng Kem', 980000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001803'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 'TW-CROP-M-XANH', 'M', 'Xanh Pastel', 980000.00, 22, 1, 0, NOW(), NOW()),

-- SP 19 (Chân váy bút chì)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-S-DEN', 'S', 'Đen', 480000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-M-DEN', 'M', 'Đen', 480000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001903'), UUID_TO_BIN('20000000-0000-0000-0000-000000000019'), 'CV-BUTCHI-M-NAU', 'M', 'Nâu Camel', 480000.00, 25, 1, 0, NOW(), NOW()),

-- SP 20 (Áo len cashmere)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-S-BE', 'S', 'Be Yến Mạch', 820000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-M-BE', 'M', 'Be Yến Mạch', 820000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002003'), UUID_TO_BIN('20000000-0000-0000-0000-000000000020'), 'LEN-CASH-L-NAU', 'L', 'Nâu Mocha', 820000.00, 20, 1, 0, NOW(), NOW()),

-- SP 21 (Đầm suông linen)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-M-TRANG', 'M', 'Trắng Tinh', 750000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 'DL-SUONG-L-XANH', 'L', 'Xanh Cốm', 750000.00, 25, 1, 0, NOW(), NOW()),

-- SP 22 (Quần tây nữ ống rộng)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-S-XAM', 'S', 'Xám Khói', 620000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000022'), 'QT-ONGRONG-M-DEN', 'M', 'Đen', 620000.00, 40, 1, 0, NOW(), NOW()),

-- SP 23 (Sơ mi tay phồng bèo)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-S-TRANG', 'S', 'Trắng Kem', 560000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000023'), 'SM-BEO-M-HONG', 'M', 'Hồng Nude', 560000.00, 25, 1, 0, NOW(), NOW()),

-- SP 24 (Trench coat nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-S-BE', 'S', 'Be Cổ Điển', 1680000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000024'), 'TC-LON-M-DEN', 'M', 'Đen', 1680000.00, 20, 1, 0, NOW(), NOW()),

-- SP 25 (Set tweed)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-S-DO', 'S', 'Đỏ Burgundy', 1390000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000025'), 'SET-TWEED-M-XANH', 'M', 'Xanh Baby', 1390000.00, 22, 1, 0, NOW(), NOW()),

-- SP 26 (Đầm maxi boho)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), 'MAXI-BOHO-F-VANG', 'Freesize', 'Vàng Mù Tạt', 690000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000026'), 'MAXI-BOHO-F-XANH', 'Freesize', 'Xanh Mint', 690000.00, 25, 1, 0, NOW(), NOW()),

-- SP 27 (Bộ suit nam navy)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-48-NAVY', '48 (M)', 'Xanh Navy', 2650000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-50-NAVY', '50 (L)', 'Xanh Navy', 2650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000027'), 'SUIT-NAVY-52-NAVY', '52 (XL)', 'Xanh Navy', 2650000.00, 10, 1, 0, NOW(), NOW()),

-- SP 28 (Sơ mi linen nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-M-TRANG', 'M', 'Trắng', 580000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 'SM-LINEN-L-DENIM', 'L', 'Xanh Denim', 580000.00, 35, 1, 0, NOW(), NOW()),

-- SP 29 (Áo biker da nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000002901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-M-DEN', 'M', 'Đen', 2250000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000002902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000029'), 'AK-BIKER-L-NAU', 'L', 'Nâu Socola', 2250000.00, 12, 1, 0, NOW(), NOW()),

-- SP 30 (Quần chinos nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-30-BE', '30', 'Be Khaki', 550000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'QT-CHINO-32-REU', '32', 'Xanh Rêu', 550000.00, 30, 1, 0, NOW(), NOW()),

-- SP 31 (Áo thun Supima)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-M-TRANG', 'M', 'Trắng', 380000.00, 60, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000031'), 'AT-SUPIMA-L-DEN', 'L', 'Đen', 380000.00, 50, 1, 0, NOW(), NOW()),

-- SP 32 (Hoodie nam streetwear)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-M-XAM', 'M', 'Xám Tiêu', 650000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000032'), 'HD-STREET-L-DEN', 'L', 'Đen', 650000.00, 40, 1, 0, NOW(), NOW()),

-- SP 33 (Áo len cable knit nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-M-KEM', 'M', 'Kem', 720000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000033'), 'LEN-CABLE-L-THAN', 'L', 'Xanh Than', 720000.00, 30, 1, 0, NOW(), NOW()),

-- SP 34 (Áo khoác bomber gió nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-M-REU', 'M', 'Xanh Rêu', 780000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000034'), 'AK-BOMBER-L-DEN', 'L', 'Đen', 780000.00, 25, 1, 0, NOW(), NOW()),

-- SP 35 (Quần jean vintage nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-30-XANH', '30', 'Xanh Nhạt', 680000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000035'), 'JN-VINTAGE-32-CHAM', '32', 'Xanh Chàm', 680000.00, 40, 1, 0, NOW(), NOW()),

-- SP 36 (Áo polo jacquard nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-M-TRANG', 'M', 'Trắng Phối Xanh', 490000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000036'), 'PL-JACQUARD-L-DEN', 'L', 'Đen Phối Vàng', 490000.00, 35, 1, 0, NOW(), NOW()),

-- SP 37 (Chelsea boot nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-40-NAU', '40', 'Nâu Sáp', 1450000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-41-NAU', '41', 'Nâu Sáp', 1450000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003703'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 'CB-SAP-42-DEN', '42', 'Đen', 1450000.00, 20, 1, 0, NOW(), NOW()),

-- SP 38 (Sandal nữ đính đá)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-38-BAC', '36', 'Ánh Bạc', 590000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-37-BAC', '37', 'Ánh Bạc', 590000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003803'), UUID_TO_BIN('20000000-0000-0000-0000-000000000038'), 'SD-MANH-38-DEN', '38', 'Đen', 590000.00, 22, 1, 0, NOW(), NOW()),

-- SP 39 (Chunky sneaker nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000003901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-36-TRANG', '36', 'Trắng Phối Hồng', 780000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000003902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 'SNK-CHUNKY-37-TRANG', '37', 'Trắng Phối Hồng', 780000.00, 35, 1, 0, NOW(), NOW()),

-- SP 40 (Derby nam da bóng)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-40-DEN', '40', 'Đen', 1350000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'DB-OFFICE-41-DEN', '41', 'Đen', 1350000.00, 25, 1, 0, NOW(), NOW()),

-- SP 41 (Giày búp bê nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-36-BE', '36', 'Be', 450000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000041'), 'FL-BOW-37-BE', '37', 'Be', 450000.00, 35, 1, 0, NOW(), NOW()),

-- SP 42 (Boot nữ gót nhọn)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-36-DEN', '36', 'Đen', 890000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000042'), 'BT-ANKLE-37-DEN', '37', 'Đen', 890000.00, 25, 1, 0, NOW(), NOW()),

-- SP 43 (Penny loafer nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-40-NAU', '40', 'Nâu Hạt Dẻ', 1150000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000043'), 'LF-PENNY-41-NAU', '41', 'Nâu Hạt Dẻ', 1150000.00, 25, 1, 0, NOW(), NOW()),

-- SP 44 (Mule sục nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-36-KEM', '36', 'Trắng Kem', 520000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000044'), 'ML-SQUARE-37-KEM', '37', 'Trắng Kem', 520000.00, 30, 1, 0, NOW(), NOW()),

-- SP 45 (Sneaker da lộn nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-40-XAM', '40', 'Xám Khói', 850000.00, 28, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000045'), 'SNK-SUEDE-41-XAM', '41', 'Xám Khói', 850000.00, 35, 1, 0, NOW(), NOW()),

-- SP 46 (Dép đế trấu nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-40-NAU', '40', 'Nâu Đất', 490000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000046'), 'DP-TRAU-41-NAU', '41', 'Nâu Đất', 490000.00, 35, 1, 0, NOW(), NOW()),

-- SP 47 (Đồng hồ chronograph nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), 'DH-CHRONO-40-NAU', 'Mặt 40mm', 'Dây Nâu Mặt Trắng', 1850000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000047'), 'DH-CHRONO-40-DEN', 'Mặt 40mm', 'Dây Đen Mặt Đen', 1850000.00, 20, 1, 0, NOW(), NOW()),

-- SP 48 (Đồng hồ pha lê nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), 'DH-CRYSTAL-28-VANG', 'Mặt 28mm', 'Vàng Hồng', 1650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000048'), 'DH-CRYSTAL-28-BAC', 'Mặt 28mm', 'Ánh Bạc', 1650000.00, 18, 1, 0, NOW(), NOW()),

-- SP 49 (Kính mát unisex)
(UUID_TO_BIN('30000000-0000-0000-0000-000000004901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 'KM-POLAR-FREE-DEN', 'Freesize', 'Đen Bóng', 680000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000004902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 'KM-POLAR-FREE-NAU', 'Freesize', 'Đồi Mồi Nâu', 680000.00, 30, 1, 0, NOW(), NOW()),

-- SP 50 (Túi tote da nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 'TOTE-LEATHER-38-DEN', '38x30cm', 'Đen', 1550000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 'TOTE-LEATHER-38-NAU', '38x30cm', 'Nâu Bò', 1550000.00, 25, 1, 0, NOW(), NOW()),

-- SP 51 (Balo da nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000051'), 'BP-WATER-42-DEN', '42x30cm', 'Đen Mờ', 1120000.00, 30, 1, 0, NOW(), NOW()),

-- SP 52 (Cà vạt lụa nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), 'CV-SILK-7-NAVY', '7x145cm', 'Xanh Navy Họa Tiết', 420000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000052'), 'CV-SILK-7-DO', '7x145cm', 'Đỏ Rượu Chìm', 420000.00, 40, 1, 0, NOW(), NOW()),

-- SP 53 (Khuyên tai ngọc trai nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000053'), 'KT-PEARL-8-BAC', '8mm', 'Bạc Ánh Kim', 390000.00, 60, 1, 0, NOW(), NOW()),

-- SP 54 (Vòng tay titan nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000054'), 'VT-TITAN-20-DEN', '20cm', 'Đen', 320000.00, 45, 1, 0, NOW(), NOW()),

-- SP 55 (Mũ beret nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), 'MU-BERET-FREE-DEN', 'Freesize', 'Đen', 290000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000055'), 'MU-BERET-FREE-BE', 'Freesize', 'Be Kem', 290000.00, 40, 1, 0, NOW(), NOW()),

-- SP 56 (Ví mini nam RFID)
(UUID_TO_BIN('30000000-0000-0000-0000-000000005601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 'VI-RFID-10-NAU', '10x8cm', 'Nâu Cà Phê', 360000.00, 45, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000005602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 'VI-RFID-10-DEN', '10x8cm', 'Đen', 360000.00, 40, 1, 0, NOW(), NOW());

-- 3. PRODUCT IMAGES
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

-- 4. REVIEWS
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

-- 5. PROMOTION PRODUCTS (Thêm một số sản phẩm vào đợt Sale)
INSERT IGNORE INTO promotion_products (promotion_id, product_id, sale_price, original_price, discount_percent) VALUES
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000018'), 784000.00, 980000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000021'), 600000.00, 750000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000028'), 464000.00, 580000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 440000.00, 550000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000037'), 1160000.00, 1450000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000039'), 624000.00, 780000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000049'), 544000.00, 680000.00, 20),
(UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000056'), 288000.00, 360000.00, 20);
