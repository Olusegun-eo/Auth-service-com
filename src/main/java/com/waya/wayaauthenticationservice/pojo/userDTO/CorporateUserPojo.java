package com.waya.wayaauthenticationservice.pojo.userDTO;


import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorporateUserPojo extends BaseUserPojo {

	@NotBlank(message = "please enter your organisation City")
	private String city;

	@NotBlank(message = "please enter your organisation Address")
	private String officeAddress;

	@NotBlank(message = "please enter your organisation State")
	private String state;

	@NotBlank(message = "please enter your organisation name")
	private String orgName;

	@NotBlank(message = "please enter your organisation Email")
	private String orgEmail;

	@NotBlank(message = "please enter your organisation Phone Number")
	private String orgPhone;

	@NotBlank(message = "please enter your organisation type")
	private String orgType;

	@NotBlank(message = "Business Type Cannot be null or blank")
	private String businessType;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(businessType, orgEmail, orgPhone);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CorporateUserPojo)) {
			return false;
		}
		CorporateUserPojo other = (CorporateUserPojo) obj;
		return Objects.equals(businessType, other.businessType) && Objects.equals(orgEmail, other.orgEmail)
				&& Objects.equals(orgPhone, other.orgPhone);
	}
	
}