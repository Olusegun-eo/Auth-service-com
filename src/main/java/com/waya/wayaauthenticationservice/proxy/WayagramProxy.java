package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.userDTO.UserIDPojo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateWayagram;
import com.waya.wayaauthenticationservice.response.SuccessResponse;

@FeignClient(name="WAYAGRAM-PROFILE", url = "${app.config.wayagram-profile.base-url}", configuration = AuthClientConfiguration.class)
public interface WayagramProxy {

	@PostMapping("/create")
	SuccessResponse createWayagramProfile(@RequestBody CreateWayagram createWayagram, @RequestHeader("Authorization") String token);

	@PutMapping("/activate-or-deactivate-wayagram-account")
	ResponseEntity<?> toggleActivation(@RequestBody UserIDPojo userIdPojo, @RequestHeader("Authorization") String token);
}
