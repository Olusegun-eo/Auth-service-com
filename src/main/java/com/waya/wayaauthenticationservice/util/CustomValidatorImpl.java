package com.waya.wayaauthenticationservice.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.waya.wayaauthenticationservice.util.HelperUtils.*;

public class CustomValidatorImpl implements ConstraintValidator<CustomValidator, String> {

    protected Type type;
    private int maxSize;
    private int minSize;
    private String[] values;

    @Override
    public void initialize(CustomValidator constraintAnnotation) {
        this.type = constraintAnnotation.type();
        this.maxSize = constraintAnnotation.max();
        this.minSize = constraintAnnotation.min();
        this.values = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null)
            return true;

        switch (this.type) {
            case SIZE:
                return validateForSize(value);
            case TEXT_STRING:
                return validateStringTextOnly(value);
            case NUMERIC_STRING:
                return validateStringNumericOnly(value);
            case EMAIL:
                return validateStringIsEmail(value);
            case CONTAINS:
                return validateContains(value);
            case EMAIL_OR_PHONENUMBER:
                return validateStringIsEmailOrPhoneNumber(value);
        }
        return false;
    }

    private boolean validateContains(String value) {
        for (String val : this.values) {
            if (!value.toLowerCase().contains(val.toLowerCase()))
                return false;
        }
        return true;
    }

    private boolean validateForSize(String value) {
        return (value.length() > this.minSize - 1) && (value.length() <= this.maxSize);
    }
}
