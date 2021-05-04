package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.entity.Users;
import feign.Headers;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "profile-service", url = "http://46.101.41.187:8080")
public interface ProfileProxy {
//    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    @PostMapping("/service/personal-profile")
    void create(Users users);
}
