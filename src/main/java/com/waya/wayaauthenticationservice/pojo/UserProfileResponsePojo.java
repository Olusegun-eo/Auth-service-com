package com.waya.wayaauthenticationservice.pojo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "userResponse", itemRelation = "user")
@JsonInclude(Include.NON_NULL)
public class UserProfileResponsePojo extends RepresentationModel<UserProfileResponsePojo> {
	
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
	
	private boolean pinCreated;

	private boolean isCorporate;
	
	@Builder.Default
	private Set<String> roles = new HashSet<>();
	
	@Builder.Default
	private Set<String> permits = new HashSet<>();


}
