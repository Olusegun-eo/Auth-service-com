package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

	@Size(min = 5, message = "Business Type should be at least 5 characters long")
	@NotBlank(message = "Business Type Cannot be null or blank")
	private String businessType;

	@NotNull(message="Kindly provide valid UserId")
	private Long userId;
}
