package com.lyrashop.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String canonicalEmail);

    @Query("""
            select user
            from User user
            where user.id = :id
              and user.active = true
            """)
    Optional<User> findActiveById(@Param("id") UUID id);

    boolean existsByEmail(String canonicalEmail);

    long countByRole(UserRole role);
}
