package com.waya.wayaauthenticationservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.waya.wayaauthenticationservice.util.HelperUtils.numericPattern;

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

        return numericPattern.matcher(phoneNumberField).find()
                && phoneNumberField.startsWith("234")
                && phoneNumberField.length() == 13;

    }
}
