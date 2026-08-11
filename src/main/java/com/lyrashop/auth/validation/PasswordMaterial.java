package com.lyrashop.auth.validation;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public final class PasswordMaterial {

    public static final int MAX_BCRYPT_UTF8_BYTES = 72;

    private PasswordMaterial() {
    }

    public static boolean isBcryptCompatible(String value) {
        if (value == null || value.length() > MAX_BCRYPT_UTF8_BYTES) {
            return false;
        }
        try {
            int utf8Length = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value))
                    .remaining();
            return utf8Length <= MAX_BCRYPT_UTF8_BYTES;
        } catch (CharacterCodingException exception) {
            return false;
        }
    }
}
