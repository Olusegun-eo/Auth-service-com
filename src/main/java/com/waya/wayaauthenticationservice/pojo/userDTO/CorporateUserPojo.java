package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorporateUserPojo extends BaseUserPojo {

	private String city;
	private String officeAddress;
	private String state;
	private String orgName;
	private String orgEmail;
	private String orgPhone;
	private String orgType;

	@Size(min = 5, message = "Business Type should be at least 5 characters long")
	@NotBlank(message = "Business Type Cannot be null or blank")
	private String businessType;

	private Long userId = 0L;

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
