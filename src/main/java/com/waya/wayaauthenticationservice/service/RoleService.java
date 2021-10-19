package com.waya.wayaauthenticationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.pojo.access.RoleAccessResponse;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessPojo;
import com.waya.wayaauthenticationservice.proxy.RoleProxy;

@Component
public class RoleService {
	
	@Autowired
	private RoleProxy roleProxy;
	
	
	public UserAccessPojo getAccess(Long userId) {
		String key = "WAYA855##0AUTH";
		System.out.println(userId);
		UserAccessPojo user = null;
		RoleAccessResponse pojo = roleProxy.GetUserAccess(userId, key);
		user = pojo.getData();
		return user;
	}

}
