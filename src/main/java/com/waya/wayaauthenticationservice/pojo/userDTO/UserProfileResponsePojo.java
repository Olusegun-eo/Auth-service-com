package com.waya.wayaauthenticationservice.pojo.userDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.HashSet;
import java.util.Set;

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
	private String referenceCode;
	private boolean pinCreated;
	private boolean isCorporate;

	private String gender;
	private String middleName;
	private String dateOfBirth;
	private String profileImage;
	private String district;
	private String address;
	private String city;
	private String state;

	@Builder.Default
	private Set<String> roles = new HashSet<>();
	
	@Builder.Default
	private Set<String> permits = new HashSet<>();
}
