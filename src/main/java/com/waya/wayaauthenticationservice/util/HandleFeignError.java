package com.waya.wayaauthenticationservice.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.waya.wayaauthenticationservice.proxy.impl.FeignHttpExceptionHandler;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleFeignError {
	Class<? extends FeignHttpExceptionHandler> value();
}
