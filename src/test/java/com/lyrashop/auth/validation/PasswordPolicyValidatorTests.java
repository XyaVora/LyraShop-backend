package com.lyrashop.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordPolicyValidatorTests {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void measuresBcryptLimitInUtf8Bytes() {
        String threeByteCharacter = String.valueOf((char) 0x20ac);
        String fourByteCharacter = new String(Character.toChars(0x1f600));

        assertThat(validator.isValid("a".repeat(72), null)).isTrue();
        assertThat(validator.isValid("a".repeat(73), null)).isFalse();
        assertThat(validator.isValid("a".repeat(100_000), null)).isFalse();
        assertThat(validator.isValid(threeByteCharacter.repeat(24), null)).isTrue();
        assertThat(validator.isValid(threeByteCharacter.repeat(25), null)).isFalse();
        assertThat(validator.isValid(fourByteCharacter.repeat(18), null)).isTrue();
        assertThat(validator.isValid(fourByteCharacter.repeat(19), null)).isFalse();
    }

    @Test
    void rejectsShortAndMalformedPasswordsWithoutTransformingInput() {
        assertThat(validator.isValid("short", null)).isFalse();
        assertThat(validator.isValid("  password with spaces  ", null)).isTrue();
        assertThat(validator.isValid("\uD800" + "a".repeat(20), null)).isFalse();
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
