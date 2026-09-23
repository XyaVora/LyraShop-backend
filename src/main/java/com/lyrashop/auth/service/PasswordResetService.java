package com.lyrashop.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.auth.entity.*;
import com.lyrashop.auth.repository.*;
import com.lyrashop.config.PasswordResetProperties;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;
import com.lyrashop.messaging.service.EmailOutboxService;

@Service
public class PasswordResetService {
 private final UserRepository users; private final PasswordResetTokenRepository tokens; private final RefreshSessionRepository sessions;
 private final RefreshTokenGenerator generator; private final BoundedPasswordOperations passwords; private final EmailOutboxService mail; private final PasswordResetProperties properties;
 public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, RefreshSessionRepository sessions,
   RefreshTokenGenerator generator, BoundedPasswordOperations passwords, EmailOutboxService mail, PasswordResetProperties properties){
  this.users=users;this.tokens=tokens;this.sessions=sessions;this.generator=generator;this.passwords=passwords;this.mail=mail;this.properties=properties;
 }
 @Transactional
 public void request(String email){
  User user;
  try { user=users.findByEmail(User.canonicalizeEmail(email)).filter(User::isActive).orElse(null); }
  catch(IllegalArgumentException e){ return; }
  if(user==null)return;
  if(tokens.existsByUserIdAndCreatedAtAfter(user.getId(), Instant.now().minus(properties.requestCooldown())))return;
  tokens.invalidateAll(user.getId());
  String raw=generator.generate();
  tokens.saveAndFlush(PasswordResetToken.issue(user.getId(),RefreshTokenDigest.fromRawToken(raw).bytes(),Instant.now().plus(properties.tokenTtl())));
  mail.enqueue(properties.fromAddress(), user.getEmail(), "Đặt lại mật khẩu LyraShop",
    "Mở liên kết sau để đặt lại mật khẩu (liên kết chỉ dùng một lần):\n"+properties.frontendUrl()+"?resetToken="+URLEncoder.encode(raw,StandardCharsets.UTF_8));
 }
 @Transactional
 public void reset(String rawToken,String newPassword){
  PasswordResetToken token;
  try { token=tokens.findActiveForUpdate(RefreshTokenDigest.fromRawToken(rawToken).bytes()).orElseThrow(InvalidPasswordResetTokenException::new); }
  catch(IllegalArgumentException e){ throw new InvalidPasswordResetTokenException(); }
  User user=users.findActiveById(token.getUserId()).orElseThrow(InvalidPasswordResetTokenException::new);
  token.use(); user.changePasswordHash(passwords.hash(newPassword)); users.saveAndFlush(user); tokens.save(token); sessions.revokeAllByUserId(user.getId());
 }
}
