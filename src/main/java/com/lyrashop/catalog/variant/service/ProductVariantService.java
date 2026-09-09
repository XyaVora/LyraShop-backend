package com.lyrashop.catalog.variant.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.dto.AdjustProductVariantInventoryRequest;
import com.lyrashop.catalog.variant.dto.CreateProductVariantRequest;
import com.lyrashop.catalog.variant.dto.UpdateProductVariantRequest;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;

@Service
public class ProductVariantService {
 private final ProductVariantRepository variants; private final ProductRepository products;
 public ProductVariantService(ProductVariantRepository variants, ProductRepository products){this.variants=variants;this.products=products;}
 @Transactional public ProductVariant create(UUID productId, CreateProductVariantRequest r){
  if(!products.existsByIdAndActiveTrue(productId)) throw new VariantProductNotFoundException();
  String sku=normalizeSku(r.sku());
  if(variants.existsBySku(sku)) throw new VariantSkuAlreadyExistsException();
  try{return variants.saveAndFlush(ProductVariant.create(productId,r.sku(),r.size(),r.color(),r.price(),r.stock()));}
  catch(DataIntegrityViolationException e){ if(isSku(e)) throw new VariantSkuAlreadyExistsException(e); throw e; }
 }
 @Transactional public ProductVariant update(UUID productId, UUID variantId, UpdateProductVariantRequest r){
  if(!products.existsByIdAndActiveTrue(productId)) throw new VariantProductNotFoundException();
  ProductVariant variant=variants.findByIdAndProductId(variantId,productId).orElseThrow(VariantNotFoundException::new);
  if(variant.getVersion()!=r.version()) throw new VariantVersionConflictException();
  String sku=normalizeSku(r.sku());
  if(variants.existsBySkuAndIdNot(sku,variantId)) throw new VariantSkuAlreadyExistsException();
  variant.updateCatalog(r.sku(),r.size(),r.color(),r.price());
  try{return variants.saveAndFlush(variant);}
  catch(DataIntegrityViolationException e){if(isSku(e))throw new VariantSkuAlreadyExistsException(e);throw e;}
  catch(ObjectOptimisticLockingFailureException e){throw new VariantVersionConflictException(e);}
 }
 @Transactional public void deactivate(UUID productId, UUID variantId){
  if(!products.existsById(productId)) throw new VariantProductNotFoundException();
  ProductVariant variant=variants.findForDeactivation(productId,variantId).orElseThrow(VariantNotFoundException::new);
  if(!variant.isActive()) return;
  variant.deactivate();
  variants.saveAndFlush(variant);
 }
 @Transactional public void activate(UUID productId, UUID variantId){
  if(!products.existsById(productId)) throw new VariantProductNotFoundException();
  ProductVariant variant=variants.findForDeactivation(productId,variantId).orElseThrow(VariantNotFoundException::new);
  if(variant.isActive()) return;
  variant.activate();
  variants.saveAndFlush(variant);
 }
 @Transactional public ProductVariant adjustInventory(UUID productId, UUID variantId, AdjustProductVariantInventoryRequest r){
  if(!products.existsById(productId)) throw new VariantProductNotFoundException();
  ProductVariant variant=variants.findByIdAndProductId(variantId,productId).orElseThrow(VariantNotFoundException::new);
  if(variant.getVersion()!=r.version()) throw new VariantVersionConflictException();
  variant.adjustInventory(r.stock());
  try{return variants.saveAndFlush(variant);}
  catch(ObjectOptimisticLockingFailureException e){throw new VariantVersionConflictException(e);}
 }
 @Transactional(readOnly = true) public List<ProductVariantResult> listActiveForProduct(UUID productId){
  return variants.findAllByProductIdAndActiveTrueOrderBySkuAscIdAsc(productId).stream().map(ProductVariantResult::from).toList();
 }
 @Transactional(readOnly = true) public List<ProductVariant> listForAdmin(UUID productId){
  return variants.findAllByProductIdOrderBySkuAscIdAsc(productId);
 }
 private static String normalizeSku(String sku){return sku.strip().toUpperCase(Locale.ROOT);}
 private static boolean isSku(Throwable e){while(e!=null){if(e.getMessage()!=null&&e.getMessage().toLowerCase(Locale.ROOT).contains("uk_product_variants_sku"))return true;e=e.getCause();}return false;}
}
