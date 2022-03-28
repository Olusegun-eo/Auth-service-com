package com.waya.wayaauthenticationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.pojo.access.RoleAccessResponse;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessPojo;
import com.waya.wayaauthenticationservice.proxy.RoleProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RoleService {
	
	@Autowired
	private RoleProxy roleProxy;
	
	
	public UserAccessPojo getAccess(Long userId) {
		UserAccessPojo user = null;
		try {
		String key = "WAYA855##0AUTH";
		System.out.println(userId);
		RoleAccessResponse pojo = roleProxy.GetUserAccess(userId, key);
		user = pojo.getData();
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
		}
		return user;
	}

}
