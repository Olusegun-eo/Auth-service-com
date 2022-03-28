package com.waya.wayaauthenticationservice.pojo.access;

import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleAccessResponse {
	
	private Date timestamp;
	private String message;
	private boolean status;
	private UserAccessPojo data;

}
