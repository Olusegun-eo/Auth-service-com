package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "profile-service", url = "http://46.101.41.187:8080/")
public interface ProfileProxy {
}
