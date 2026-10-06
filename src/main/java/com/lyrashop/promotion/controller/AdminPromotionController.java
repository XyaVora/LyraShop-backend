package com.lyrashop.promotion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.lyrashop.common.dto.PageResponse;
import com.lyrashop.common.web.AdminPageable;

import com.lyrashop.promotion.dto.PromotionRequest;
import com.lyrashop.promotion.dto.PromotionResponse;
import com.lyrashop.promotion.service.PromotionService;

import jakarta.validation.Valid;

@RestController @RequestMapping("/api/v1/admin/promotions") @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
public class AdminPromotionController {
    private final PromotionService service;
    public AdminPromotionController(PromotionService service) { this.service = service; }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE) public List<PromotionResponse> list() { return service.list(); }
    @GetMapping(path="/page", produces=MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<PromotionResponse> page(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) String query,@RequestParam(required=false) Boolean active,
            @RequestParam(defaultValue="startsAt") String sort,@RequestParam(defaultValue="desc") String direction){var result=service.page(query,active,AdminPageable.of(page,size,sort,direction,java.util.Set.of("name","discountPercent","startsAt","endsAt","active"),"startsAt"));return PageResponse.from(result,result.getContent());}
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PromotionResponse update(@PathVariable UUID id, @Valid @RequestBody PromotionRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
