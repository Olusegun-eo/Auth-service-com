package com.waya.wayaauthenticationservice.proxy.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.exception.CustomException;

import feign.Response;

@Component
public class ApiClientExceptionHandler implements FeignHttpExceptionHandler {
	
	@Override
	public Exception handle(Response response) {
		HttpStatus httpStatus = HttpStatus.resolve(response.status());
		String body = response.body().toString();
		/*
		 * if (HttpStatus.NOT_FOUND.equals(httpStatus)) { return new
		 * CustomException(body, httpStatus); }
		 */
		return new CustomException(body, httpStatus);
	}
}