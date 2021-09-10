package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfilePojo {

	private Long id;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createdAt;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updatedAt;
	private String address;
	private Integer age;
	private String city;
	private Boolean isCorporate;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date dateOfBirth;
	private Boolean isDeleted;
	private String district;
	private String email;
	private String firstName;
	private String lastName;
	private String phoneNo;
	private String middleName;
	private String gender;
	private String organisationName;
	private String referral;
	private String profileImage;
	private String state;
	private String qrCode;
	private String createdBy;
	private String modifiedBy;

	private boolean isAdmin;
	private boolean isPhoneVerified;
	private boolean isAccountLocked;
	private boolean isAccountExpired;
	private boolean isCredentialsExpired;
	private boolean isActive;
	private boolean isEmailVerified;
	private boolean pinCreated;
	private boolean isSimulated;
	private String referenceCode;
    
	@Builder.Default
	private Set<String> roles = new HashSet<>();
	@Builder.Default
	private Set<String> permits = new HashSet<>();

}
