package com.lyrashop.brand.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.brand.dto.BrandResponse;

@RestController @RequestMapping("/api/v1/brand")
public class BrandController {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public BrandResponse get() {
        return new BrandResponse(
                "LYRA", "Phong cách định nghĩa bạn",
                "Từ xưởng may thủ công, LYRA theo đuổi sự hoàn hảo trong từng đường kim mũi chỉ. Mỗi bộ sưu tập kết hợp phom dáng hiện đại với chất liệu thiên nhiên thân thiện với môi trường.",
                2018, "1900 1234", "hello@lyra.vn", "128 Phố Huế, Hai Bà Trưng, Hà Nội");
    }
}
