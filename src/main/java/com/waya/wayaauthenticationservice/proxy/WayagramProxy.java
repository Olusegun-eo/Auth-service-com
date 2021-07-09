package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.CreateWayagram;
import com.waya.wayaauthenticationservice.response.SuccessResponse;

@FeignClient(name="WAYAGRAM-PROFILE", url = "http://157.245.84.14:1000/profile", configuration = AuthClientConfiguration.class)
public interface WayagramProxy {

	@PostMapping("/create")
	SuccessResponse createWayagramProfile(@RequestBody CreateWayagram createWayagram, @RequestHeader("Authorization") String token);
}
