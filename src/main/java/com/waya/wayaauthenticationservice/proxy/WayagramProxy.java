package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateWayagram;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserIDPojo;

@FeignClient(name="WAYAGRAM-PROFILE", url = "${app.config.wayagram-profile.base-url}", configuration = AuthClientConfiguration.class)
public interface WayagramProxy {

	@PostMapping("/main/profile/create")
	ResponseEntity<String> createWayagramProfile(@RequestBody CreateWayagram createWayagram);

	@PutMapping("/main/profile/activate-or-deactivate-wayagram-account")
	ResponseEntity<?> toggleActivation(@RequestBody UserIDPojo userIdPojo, @RequestHeader("Authorization") String token);

	@PutMapping("/main/profile/un-delete-wayagram-account")
	ResponseEntity<?> undeleteAccount(@RequestBody UserIDPojo userIdPojo, @RequestHeader("Authorization") String token);

	@DeleteMapping("/main/profile/delete-wayagram-account")
	ResponseEntity<?> deleteWayagramAccount(@RequestBody UserIDPojo userIdPojo, @RequestHeader("Authorization") String token);

	@PostMapping("/graph/friend/waya-auto-follow")
	ResponseEntity<String> autoFollowWayagram(@RequestBody UserIDPojo userIdPojo);
}
