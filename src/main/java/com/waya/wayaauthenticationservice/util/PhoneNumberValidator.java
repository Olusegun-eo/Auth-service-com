package com.waya.wayaauthenticationservice.util;

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
        if( phoneNumberField == null )
            return true;

        return phoneNumberField.matches("[0-9]+") && phoneNumberField.startsWith("234");
                
    }
}
