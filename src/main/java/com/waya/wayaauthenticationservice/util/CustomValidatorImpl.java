package com.waya.wayaauthenticationservice.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomValidatorImpl implements ConstraintValidator<CustomValidator, String> {

    protected Type type;
    private int maxSize;
    private int minSize;

    @Override
    public void initialize(CustomValidator constraintAnnotation) {
        this.type = constraintAnnotation.type();
        this.maxSize = constraintAnnotation.max();
        this.minSize = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if(value == null)
            return true;

        switch(this.type){
            case SIZE:
                return validateForSize(value);
            case TEXT_STRING:
                return validateStringTextOnly(value);
            case NUMERIC_STRING:
                return validateStringNumericOnly(value);
            case EMAIL:
                return validateStringIsEmail(value);
        }
        return false;
    }

    private boolean validateStringIsEmail(String value) {
        Pattern pattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
                + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    private boolean validateStringNumericOnly(String value) {
        //value.matches("[0-9]+")
        return value.matches("^[0-9]*$");
    }

    private boolean validateStringTextOnly (String value) {
        return value.matches("^[a-zA-Z]*$");
    }

    private boolean validateForSize(String value) {
        return (value.length() > this.minSize - 1) && (value.length() <= this.maxSize);
    }
}
