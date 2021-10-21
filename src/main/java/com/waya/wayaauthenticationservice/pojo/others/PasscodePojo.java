package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

@Data
public class PasscodePojo {
	
	private String email;

    private String phoneNumber;
    
    private Long roleId;
    
    private Long inviteeId;
    
    private Long invitorId;
    
    private String passcode;
    
    private String name;

	public PasscodePojo(String email, String phoneNumber, Long roleId, Long inviteeId, Long invitorId, String passcode,
			String name) {
		super();
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.roleId = roleId;
		this.inviteeId = inviteeId;
		this.invitorId = invitorId;
		this.passcode = passcode;
		this.name = name;
	}

}
