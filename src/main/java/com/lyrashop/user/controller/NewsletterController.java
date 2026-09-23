package com.lyrashop.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lyrashop.user.service.NewsletterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/v1/newsletter/subscriptions")
public class NewsletterController {
    private final NewsletterService service;
    public NewsletterController(NewsletterService service){this.service=service;}
    public record EmailRequest(@NotBlank @Email String email){}
    public record TokenRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token){}
    @PostMapping public ResponseEntity<Void> subscribe(@Valid @RequestBody EmailRequest request){service.subscribe(request.email());return ResponseEntity.accepted().build();}
    @PostMapping("/confirm") public ResponseEntity<Void> confirm(@Valid @RequestBody TokenRequest request){service.confirm(request.token());return ResponseEntity.noContent().build();}
    @PostMapping("/unsubscribe") public ResponseEntity<Void> unsubscribe(@Valid @RequestBody TokenRequest request){service.unsubscribe(request.token());return ResponseEntity.noContent().build();}
}
