package com.lyrashop.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.user.entity.CustomerSupportNote;

public interface CustomerSupportNoteRepository extends JpaRepository<CustomerSupportNote, UUID> {
    List<CustomerSupportNote> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
