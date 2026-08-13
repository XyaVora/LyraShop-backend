package com.lyrashop.catalog.variant.controller;

import java.util.UUID;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
import com.lyrashop.catalog.variant.dto.*; import com.lyrashop.catalog.variant.service.*; import jakarta.validation.Valid;

@RestController @RequestMapping("/api/v1/admin/products/{productId}/variants")
public class AdminProductVariantController {
 private final ProductVariantService service;
 public AdminProductVariantController(ProductVariantService service){this.service=service;}
 @PreAuthorize("hasRole('ADMIN')")
 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<ProductVariantResponse> create(@PathVariable String productId,@Valid @RequestBody CreateProductVariantRequest request){
  try{return ResponseEntity.status(HttpStatus.CREATED).body(ProductVariantResponse.from(service.create(UUID.fromString(productId),request)));}
  catch(IllegalArgumentException e){throw new VariantProductNotFoundException();}
 }
}
