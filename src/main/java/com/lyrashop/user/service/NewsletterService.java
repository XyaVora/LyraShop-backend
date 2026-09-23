package com.lyrashop.user.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.auth.service.RefreshTokenGenerator;
import com.lyrashop.config.NewsletterProperties;
import com.lyrashop.user.entity.NewsletterSubscription;
import com.lyrashop.user.repository.NewsletterRepository;
import com.lyrashop.messaging.service.EmailOutboxService;

@Service
public class NewsletterService {
    private final NewsletterRepository subscriptions; private final RefreshTokenGenerator tokens;
    private final EmailOutboxService mail; private final NewsletterProperties properties;
    public NewsletterService(NewsletterRepository subscriptions,RefreshTokenGenerator tokens,EmailOutboxService mail,NewsletterProperties properties){this.subscriptions=subscriptions;this.tokens=tokens;this.mail=mail;this.properties=properties;}
    @Transactional public void subscribe(String rawEmail){
        String email=rawEmail.strip().toLowerCase(Locale.ROOT);String confirmation=tokens.generate();String unsubscribe=tokens.generate();
        NewsletterSubscription subscription=subscriptions.findByEmail(email).orElseGet(()->new NewsletterSubscription(email));
        subscription.beginConfirmation(RefreshTokenDigest.fromRawToken(confirmation).bytes(),RefreshTokenDigest.fromRawToken(unsubscribe).bytes(),Instant.now().plus(properties.confirmationTtl()));subscriptions.saveAndFlush(subscription);
        String base=properties.frontendUrl();String confirmUrl=base+separator(base)+"newsletterAction=confirm&newsletterToken="+encode(confirmation);
        String unsubscribeUrl=base+separator(base)+"newsletterAction=unsubscribe&newsletterToken="+encode(unsubscribe);
        mail.enqueue(properties.fromAddress(),email,"Xác nhận đăng ký bản tin LYRA",
                "Xác nhận đăng ký bản tin tại:\n"+confirmUrl+"\n\nNếu bạn không yêu cầu đăng ký, hãy hủy tại:\n"+unsubscribeUrl);
    }
    @Transactional public void confirm(String rawToken){NewsletterSubscription subscription=subscriptions.findByConfirmationTokenHashAndConfirmationExpiresAtAfter(digest(rawToken),Instant.now()).orElseThrow(InvalidNewsletterTokenException::new);subscription.confirm();subscriptions.save(subscription);}
    @Transactional public void unsubscribe(String rawToken){NewsletterSubscription subscription=subscriptions.findByUnsubscribeTokenHash(digest(rawToken)).orElseThrow(InvalidNewsletterTokenException::new);subscription.deactivate();subscriptions.save(subscription);}
    private static byte[] digest(String token){try{return RefreshTokenDigest.fromRawToken(token).bytes();}catch(IllegalArgumentException exception){return new byte[RefreshTokenDigest.LENGTH];}}
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    private static String separator(String url){return url.contains("?")?"&":"?";}
}
