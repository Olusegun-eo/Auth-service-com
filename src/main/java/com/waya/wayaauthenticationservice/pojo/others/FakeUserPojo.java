package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

@Data
public class FakeUserPojo {
	
	private String uFirstName;
	
	private String uLastName;
	
	private String uEmail;
	
	private String uPhone;
	
	private String uAddress;
	
	private String uType;

	public FakeUserPojo(String uFirstName, String uLastName, String uEmail, String uPhone, String uAddress,
			String uType) {
		super();
		this.uFirstName = uFirstName;
		this.uLastName = uLastName;
		this.uEmail = uEmail;
		this.uPhone = uPhone;
		this.uAddress = uAddress;
		this.uType = uType;
	}

}
