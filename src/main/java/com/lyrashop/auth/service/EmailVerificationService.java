package com.lyrashop.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.entity.EmailVerificationToken;
import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.auth.repository.EmailVerificationTokenRepository;
import com.lyrashop.config.EmailVerificationProperties;
import com.lyrashop.messaging.service.EmailOutboxService;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@Service
public class EmailVerificationService {
    private final UserRepository users;
    private final EmailVerificationTokenRepository verificationTokens;
    private final RefreshTokenGenerator tokenGenerator;
    private final EmailOutboxService mail;
    private final EmailVerificationProperties properties;

    public EmailVerificationService(UserRepository users, EmailVerificationTokenRepository verificationTokens,
            RefreshTokenGenerator tokenGenerator, EmailOutboxService mail, EmailVerificationProperties properties) {
        this.users = users;
        this.verificationTokens = verificationTokens;
        this.tokenGenerator = tokenGenerator;
        this.mail = mail;
        this.properties = properties;
    }

    @Transactional
    public void issue(User user, boolean applyCooldown) {
        if (user.isEmailVerified()) return;
        Instant now = Instant.now();
        if (applyCooldown && verificationTokens.existsByUserIdAndCreatedAtAfter(user.getId(), now.minus(properties.requestCooldown()))) return;
        verificationTokens.invalidateAll(user.getId());
        String rawToken = tokenGenerator.generate();
        verificationTokens.saveAndFlush(EmailVerificationToken.issue(user.getId(),
                RefreshTokenDigest.fromRawToken(rawToken).bytes(), now.plus(properties.tokenTtl())));
        String separator = properties.frontendUrl().contains("?") ? "&" : "?";
        String url = properties.frontendUrl() + separator + "verifyEmailToken="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        mail.enqueue(properties.fromAddress(), user.getEmail(), "Xác minh email LyraShop",
                "Mở liên kết sau để xác minh tài khoản LyraShop:\n" + url
                        + "\n\nLiên kết sẽ hết hạn và chỉ sử dụng được một lần.");
    }

    @Transactional
    public void resend(String rawEmail) {
        User user;
        try {
            user = users.findByEmail(User.canonicalizeEmail(rawEmail)).filter(User::isActive).orElse(null);
        } catch (IllegalArgumentException exception) {
            return;
        }
        if (user != null) issue(user, true);
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token;
        try {
            token = verificationTokens.findActiveForUpdate(RefreshTokenDigest.fromRawToken(rawToken).bytes())
                    .orElseThrow(InvalidEmailVerificationTokenException::new);
        } catch (IllegalArgumentException exception) {
            throw new InvalidEmailVerificationTokenException();
        }
        User user = users.findById(token.getUserId()).filter(User::isActive)
                .orElseThrow(InvalidEmailVerificationTokenException::new);
        token.use();
        user.verifyEmail();
        users.saveAndFlush(user);
        verificationTokens.save(token);
    }
}
