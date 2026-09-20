package com.lyrashop.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.slf4j.*;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.auth.entity.*;
import com.lyrashop.auth.repository.*;
import com.lyrashop.config.PasswordResetProperties;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@Service
public class PasswordResetService {
 private static final Logger LOGGER=LoggerFactory.getLogger(PasswordResetService.class);
 private final UserRepository users; private final PasswordResetTokenRepository tokens; private final RefreshSessionRepository sessions;
 private final RefreshTokenGenerator generator; private final BoundedPasswordOperations passwords; private final JavaMailSender mail; private final PasswordResetProperties properties;
 public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, RefreshSessionRepository sessions,
   RefreshTokenGenerator generator, BoundedPasswordOperations passwords, JavaMailSender mail, PasswordResetProperties properties){
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
  SimpleMailMessage message=new SimpleMailMessage(); message.setFrom(properties.fromAddress()); message.setTo(user.getEmail()); message.setSubject("Đặt lại mật khẩu LyraShop");
  message.setText("Mở liên kết sau để đặt lại mật khẩu (liên kết chỉ dùng một lần):\n"+properties.frontendUrl()+"?resetToken="+URLEncoder.encode(raw,StandardCharsets.UTF_8));
  try { mail.send(message); } catch(MailException e){ LOGGER.error("Unable to send password reset email for user {}",user.getId(),e); }
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
