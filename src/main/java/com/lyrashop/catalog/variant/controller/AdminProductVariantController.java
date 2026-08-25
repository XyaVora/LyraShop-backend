package com.lyrashop.catalog.variant.controller;

import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lyrashop.catalog.variant.dto.*;
import com.lyrashop.catalog.variant.service.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/variants")
public class AdminProductVariantController {
 private final ProductVariantService service;
 public AdminProductVariantController(ProductVariantService service){this.service=service;}

 @PreAuthorize("hasRole('ADMIN')")
 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<ProductVariantResponse> create(@PathVariable String productId,@Valid @RequestBody CreateProductVariantRequest request){
  try{return ResponseEntity.status(HttpStatus.CREATED).body(ProductVariantResponse.from(service.create(UUID.fromString(productId),request)));}
  catch(IllegalArgumentException e){throw new VariantProductNotFoundException();}
 }

 @PreAuthorize("hasRole('ADMIN')")
 @PutMapping(path="/{variantId}",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<ProductVariantResponse> update(@PathVariable String productId,@PathVariable String variantId,@Valid @RequestBody UpdateProductVariantRequest request){
  try{return ResponseEntity.ok(ProductVariantResponse.from(service.update(UUID.fromString(productId),UUID.fromString(variantId),request)));}
  catch(IllegalArgumentException e){throw new VariantNotFoundException();}
 }

 @PreAuthorize("hasRole('ADMIN')")
 @PatchMapping(path="/{variantId}/deactivate")
 public ResponseEntity<Void> deactivate(@PathVariable String productId,@PathVariable String variantId){
  UUID productUuid;
  try{productUuid=UUID.fromString(productId);}catch(IllegalArgumentException e){throw new VariantProductNotFoundException();}
  UUID variantUuid;
  try{variantUuid=UUID.fromString(variantId);}catch(IllegalArgumentException e){throw new VariantNotFoundException();}
  service.deactivate(productUuid,variantUuid);
  return ResponseEntity.noContent().build();
 }

 @PreAuthorize("hasRole('ADMIN')")
 @PatchMapping(path="/{variantId}/inventory",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
 public ProductVariantInventoryResponse adjustInventory(@PathVariable String productId,@PathVariable String variantId,@Valid @RequestBody AdjustProductVariantInventoryRequest request){
  UUID productUuid;
  try{productUuid=UUID.fromString(productId);}catch(IllegalArgumentException e){throw new VariantProductNotFoundException();}
  UUID variantUuid;
  try{variantUuid=UUID.fromString(variantId);}catch(IllegalArgumentException e){throw new VariantNotFoundException();}
  return ProductVariantInventoryResponse.from(service.adjustInventory(productUuid,variantUuid,request));
 }
}
