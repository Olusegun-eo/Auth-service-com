package com.waya.wayaauthenticationservice.pojo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponsePojo {
	
	@JsonProperty("userId")
    private Long id;
    
    private String email;
    
    private boolean isEmailVerified;
    
    private String phoneNumber;
    
    private String firstName;
    
    private String lastName;
    
    private boolean isAdmin;
	
	private boolean isPhoneVerified;
	
	private boolean isAccountLocked;
	
	private boolean isAccountExpired;
	
	private boolean isCredentialsExpired;
	
	private boolean isActive;
	
	private boolean isAccountDeleted;
	
	@Builder.Default
	private List<String> roles = new ArrayList<>();

}
