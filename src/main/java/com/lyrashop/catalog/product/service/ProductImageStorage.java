package com.lyrashop.catalog.product.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.lyrashop.config.UploadProperties;

@Component
public class ProductImageStorage {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final UploadProperties properties;

    public ProductImageStorage(UploadProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidProductImageException();
        }
        if (file.getSize() > properties.maxBytes()) {
            throw new InvalidProductImageException();
        }
        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new InvalidProductImageException();
        }
        String filename = UUID.randomUUID() + "." + extension;
        Path target = properties.directoryPath().resolve(filename).normalize();
        if (!target.startsWith(properties.directoryPath())) {
            throw new InvalidProductImageException();
        }
        try {
            Files.createDirectories(properties.directoryPath());
            try (InputStream in = file.getInputStream()) {
                byte[] header = in.readNBytes(12);
                if (!matchesMagic(extension, header)) {
                    throw new InvalidProductImageException();
                }
                try (InputStream body = new SequenceInputStream(new ByteArrayInputStream(header), in)) {
                    Files.copy(body, target);
                }
            }
        } catch (InvalidProductImageException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store product image", exception);
        }
        return "/api/v1/files/" + filename;
    }

    public Resource load(String filename) {
        if (filename == null) {
            throw new InvalidProductImageException();
        }
        String safeName = filename.toLowerCase(Locale.ROOT);
        if (!safeName.matches("[0-9a-f-]{36}\\.(jpg|jpeg|png|webp)")) {
            throw new InvalidProductImageException();
        }
        Path target = properties.directoryPath().resolve(safeName).normalize();
        if (!target.startsWith(properties.directoryPath()) || !Files.isRegularFile(target)) {
            throw new InvalidProductImageException();
        }
        return new FileSystemResource(target);
    }

    private static boolean matchesMagic(String extension, byte[] header) {
        return switch (extension) {
            case "jpg" -> header.length >= 3
                    && header[0] == (byte) 0xFF
                    && header[1] == (byte) 0xD8
                    && header[2] == (byte) 0xFF;
            case "png" -> header.length >= 8
                    && header[0] == (byte) 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;
            case "webp" -> header.length >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';
            default -> false;
        };
    }
}
