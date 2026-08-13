package com.lyrashop.catalog.variant.service;

import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.dto.CreateProductVariantRequest;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;

@Service
public class ProductVariantService {
 private final ProductVariantRepository variants; private final ProductRepository products;
 public ProductVariantService(ProductVariantRepository variants, ProductRepository products){this.variants=variants;this.products=products;}
 @Transactional public ProductVariant create(UUID productId, CreateProductVariantRequest r){
  if(!products.existsByIdAndActiveTrue(productId)) throw new VariantProductNotFoundException();
  String sku=r.sku().strip().toUpperCase(java.util.Locale.ROOT);
  if(variants.existsBySku(sku)) throw new VariantSkuAlreadyExistsException();
  try{return variants.saveAndFlush(ProductVariant.create(productId,r.sku(),r.size(),r.color(),r.price(),r.stock()));}
  catch(DataIntegrityViolationException e){ if(isSku(e)) throw new VariantSkuAlreadyExistsException(e); throw e; }
 }
 private static boolean isSku(Throwable e){ while(e!=null){if(e.getMessage()!=null&&e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("uk_product_variants_sku"))return true;e=e.getCause();}return false;}
}
