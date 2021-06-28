package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;

@Data
public class DevicePojo {
	
	private String deviceType;
	
	private String platform;

	public DevicePojo(String deviceType, String platform) {
		super();
		this.deviceType = deviceType;
		this.platform = platform;
	}
	

}
