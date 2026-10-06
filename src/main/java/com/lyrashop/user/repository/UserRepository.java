package com.lyrashop.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

import jakarta.persistence.LockModeType;

import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String canonicalEmail);

    @Query("""
            select user
            from User user
            where user.id = :id
              and user.active = true
            """)
    Optional<User> findActiveById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByEmail(String canonicalEmail);

    long countByRole(UserRole role);
    long countByRoleAndActiveTrue(UserRole role);

    @Query("""
            select user from User user
            where lower(user.email) like lower(concat('%', :query, '%'))
               or lower(user.fullName) like lower(concat('%', :query, '%'))
               or lower(coalesce(user.phone, '')) like lower(concat('%', :query, '%'))
            order by user.updatedAt desc, user.id desc
            """)
    List<User> searchForAdmin(@Param("query") String query, Pageable pageable);
}
