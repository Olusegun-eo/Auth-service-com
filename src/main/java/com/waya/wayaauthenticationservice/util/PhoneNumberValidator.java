package com.waya.wayaauthenticationservice.util;

import static com.waya.wayaauthenticationservice.util.HelperUtils.phoneNumPattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//@Configurable

public class PhoneNumberValidator implements ConstraintValidator<ValidPhone, String> {
 
    @Override
    public void initialize(ValidPhone validPhone) {
    }

    @Override
    public boolean isValid(String phoneNumberField, ConstraintValidatorContext context) {
        // null values are valid
        if(phoneNumberField == null)
            return true;
        if(phoneNumberField.startsWith("+"))
        	phoneNumberField = phoneNumberField.substring(1).replaceAll("\\s+", "").trim();
        
        return phoneNumPattern.matcher(phoneNumberField).find()
                && phoneNumberField.startsWith("234")
                && phoneNumberField.length() >= 13;

    }
}
