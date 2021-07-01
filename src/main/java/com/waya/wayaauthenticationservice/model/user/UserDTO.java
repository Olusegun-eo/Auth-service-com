package com.waya.wayaauthenticationservice.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserDTO {
	
	@JsonProperty("userId")
	private Long id;
	
	private boolean accountNonLocked;
	private boolean deleted;
	private String email;
	private boolean emailVerified;
	private boolean isActive;
	private String phoneNumber;
	
	private boolean phoneVerified;
	
	private String firstName;
	
	@JsonProperty("lastName")
	private String surname;
}
