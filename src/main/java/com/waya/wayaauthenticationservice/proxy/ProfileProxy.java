package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import com.waya.wayaauthenticationservice.entity.Users;

import feign.Headers;

@FeignClient(name = "profile-service", url = "${app.config.profile.base-url}")
public interface ProfileProxy {
//    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    @PostMapping("/service/personal-profile")
    void create(Users users);
}
