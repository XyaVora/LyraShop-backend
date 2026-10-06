package com.lyrashop.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.user.entity.CustomerSupportNote;

public record CustomerSupportNoteResponse(
        UUID id,
        String note,
        UUID createdBy,
        String createdByName,
        Instant createdAt
) {
    public static CustomerSupportNoteResponse from(CustomerSupportNote note, String createdByName) {
        return new CustomerSupportNoteResponse(note.getId(), note.getNote(), note.getCreatedBy(), createdByName, note.getCreatedAt());
    }
}
