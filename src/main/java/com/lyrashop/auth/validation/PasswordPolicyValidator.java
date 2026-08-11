package com.lyrashop.auth.validation;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<ValidPassword, String> {

    static final int MIN_CODE_POINTS = 12;
    static final int MAX_UTF8_BYTES = 72;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.length() > MAX_UTF8_BYTES) {
            return false;
        }
        if (value.codePointCount(0, value.length()) < MIN_CODE_POINTS) {
            return false;
        }

        try {
            int utf8Length = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value))
                    .remaining();
            return utf8Length <= MAX_UTF8_BYTES;
        } catch (CharacterCodingException exception) {
            return false;
        }
    }
}
