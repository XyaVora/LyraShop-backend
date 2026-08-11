package com.lyrashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class LyraShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyraShopApplication.class, args);
    }
}
