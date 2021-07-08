package com.waya.wayaauthenticationservice.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidatorImpl.class)
@Repeatable(RepeatableCustomValidator.class)
public @interface CustomValidator {

    String message() default "";

    int min() default 1;

    int max() default Integer.MAX_VALUE;

    String format() default "dd-MM-yyyy";

    String[] values() default {};

    Type type();

    boolean optional() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
