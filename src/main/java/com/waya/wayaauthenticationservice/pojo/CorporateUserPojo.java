package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorporateUserPojo extends BaseUserPojo{

	private String city;

	private String officeAddress;

	private String state;
	private String orgName;
	private String orgEmail;
	private String orgPhone;
	private String orgType;
	private String businessType;

	private Long userId;

}
