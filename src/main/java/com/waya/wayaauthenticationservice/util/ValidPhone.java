package com.waya.wayaauthenticationservice.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhone {

    String message() default "Ensure phone number is valid and starts with 234";
    Class<?>[] groups() default {};
    //String value();
    Class<? extends Payload>[] payload() default {};
}
