package com.waya.wayaauthenticationservice.pojo.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessPojo {
	
	private Long id;
	private String phoneNumber;
	private String email;
	private Long userId;
	private RoleAccessPojo role;
	
	

}
