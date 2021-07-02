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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "productResponse", itemRelation = "item")
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
