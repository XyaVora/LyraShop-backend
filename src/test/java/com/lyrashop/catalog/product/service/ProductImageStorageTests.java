package com.lyrashop.catalog.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.lyrashop.config.UploadProperties;

class ProductImageStorageTests {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};
    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
    };

    @TempDir
    Path directory;

    @Test
    void storesAllowedImagesAndRejectsSpoofedTypes() throws Exception {
        ProductImageStorage storage = new ProductImageStorage(
                new UploadProperties(directory.toString(), 2097152)
        );
        String jpegUrl = storage.store(new MockMultipartFile(
                "file", "shirt.jpg", "image/jpeg", JPEG
        ));
        assertThat(jpegUrl).matches("/api/v1/files/[0-9a-f-]{36}\\.jpg");
        String filename = jpegUrl.substring("/api/v1/files/".length());
        assertThat(storage.load(filename).getContentAsByteArray()).isEqualTo(JPEG);

        String pngUrl = storage.store(new MockMultipartFile(
                "file", "shirt.png", "image/png", PNG
        ));
        assertThat(pngUrl).endsWith(".png");
        storage.delete(pngUrl);
        assertThatThrownBy(() -> storage.load(pngUrl.substring("/api/v1/files/".length())))
                .isInstanceOf(InvalidProductImageException.class);

        assertThatThrownBy(() -> storage.store(new MockMultipartFile(
                "file", "shirt.jpg", "text/plain", JPEG
        ))).isInstanceOf(InvalidProductImageException.class);
        assertThatThrownBy(() -> storage.store(new MockMultipartFile(
                "file", "shirt.jpg", "image/jpeg", "not-an-image".getBytes()
        ))).isInstanceOf(InvalidProductImageException.class);
        assertThatThrownBy(() -> storage.store(new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        ))).isInstanceOf(InvalidProductImageException.class);
        assertThatThrownBy(() -> storage.load("../secret.jpg"))
                .isInstanceOf(InvalidProductImageException.class);
        assertThatThrownBy(() -> storage.load(filename.replace(".jpg", ".png")))
                .isInstanceOf(InvalidProductImageException.class);
    }
}
