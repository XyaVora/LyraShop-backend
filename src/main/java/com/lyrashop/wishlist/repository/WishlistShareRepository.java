package com.lyrashop.wishlist.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lyrashop.wishlist.entity.WishlistShare;
public interface WishlistShareRepository extends JpaRepository<WishlistShare, UUID> {}
