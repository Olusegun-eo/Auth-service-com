package com.waya.wayaauthenticationservice.proxy.impl;

import feign.Response;

public interface FeignHttpExceptionHandler {
	Exception handle(Response response);
}
