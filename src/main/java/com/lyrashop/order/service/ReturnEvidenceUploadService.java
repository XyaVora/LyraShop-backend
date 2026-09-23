package com.lyrashop.order.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lyrashop.catalog.product.service.ProductImageStorage;
import com.lyrashop.order.entity.ReturnEvidenceUpload;
import com.lyrashop.order.repository.ReturnEvidenceUploadRepository;

@Service
public class ReturnEvidenceUploadService {
    private static final Duration STAGING_TTL = Duration.ofHours(1);
    private static final String LOCAL_PREFIX = "/api/v1/files/";
    private final ProductImageStorage storage;
    private final ReturnEvidenceUploadRepository uploads;

    public ReturnEvidenceUploadService(ProductImageStorage storage,ReturnEvidenceUploadRepository uploads){this.storage=storage;this.uploads=uploads;}

    @Transactional
    public String store(UUID userId,MultipartFile file){
        String url=storage.store(file);
        try{return uploads.saveAndFlush(ReturnEvidenceUpload.create(userId,url,Instant.now().plus(STAGING_TTL))).getUrl();}
        catch(RuntimeException exception){storage.delete(url);throw exception;}
    }

    @Transactional
    public void consume(UUID userId,List<String> urls){
        Instant now=Instant.now();
        urls.stream().filter(url->url.startsWith(LOCAL_PREFIX)).forEach(url->{
            ReturnEvidenceUpload upload=uploads.findConsumable(url,userId,now).orElseThrow(InvalidOrderStatusException::new);
            upload.consume();uploads.save(upload);
        });
    }

    @Transactional
    public int cleanupExpired(){
        List<ReturnEvidenceUpload> expired=uploads.findExpired(Instant.now(),PageRequest.of(0,100));
        expired.forEach(upload->{storage.delete(upload.getUrl());uploads.delete(upload);});
        return expired.size();
    }

    @Transactional
    public void discard(UUID userId,List<String> urls){
        urls.stream().filter(url->url.startsWith(LOCAL_PREFIX)).forEach(url->
            uploads.findByUrlAndUserId(url,userId).ifPresent(upload->{storage.delete(url);uploads.delete(upload);})
        );
    }
}
