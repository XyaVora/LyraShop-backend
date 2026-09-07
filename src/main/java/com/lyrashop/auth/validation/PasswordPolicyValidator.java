package com.lyrashop.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<ValidPassword, String> {

    public static final int MIN_CODE_POINTS = 12;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.codePointCount(0, value.length()) < MIN_CODE_POINTS) {
            return false;
        }
        return PasswordMaterial.isBcryptCompatible(value);
    }
}
