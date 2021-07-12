package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;
import com.waya.wayaauthenticationservice.util.ValidPhone;

public class BaseUserPojo {

	@NotNull(message = "Email Cannot be Null")
	@CustomValidator(message = "Invalid Email", type = Type.EMAIL)
	private String email;

	@NotBlank(message = "Phone Number Cannot be blank")
	@ValidPhone
	@CustomValidator(message = "Phone Number must be at least 13 characters", type = Type.SIZE, min = 13, max = 15)
	private String phoneNumber;

	private String referenceCode;

	@NotNull(message = "First Name Cannot be Null")
	@CustomValidator(message = "First Name must be at least 2 characters", type = Type.SIZE, min = 2)
	@CustomValidator(message = "First Name cannot Contain Non Alphabets", type = Type.TEXT_STRING)
	private String firstName;

	@NotNull(message = "SurName Cannot be Null")
	@Size(min = 2, message = "SurName must be at least 2 characters")
	@CustomValidator(message = "SurName cannot Contain Non Alphabets", type = Type.TEXT_STRING)
	private String surname;

	@NotNull(message = "Password Cannot be null")
	@Size(min = 8, message = "Password must be at least 8 characters Long")
	private String password;

	private boolean isAdmin = false;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.replaceAll("\\s+", "").trim();
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber.replaceAll("\\s+", "").trim();
	}

	public String getReferenceCode() {
		return referenceCode;
	}

	public void setReferenceCode(String referenceCode) {
		this.referenceCode = referenceCode.replaceAll("\\s+", "").trim();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName.replaceAll("\\s+", "").trim();
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname.replaceAll("\\s+", "").trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, phoneNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BaseUserPojo)) {
			return false;
		}
		BaseUserPojo other = (BaseUserPojo) obj;
		return Objects.equals(email, other.email) && Objects.equals(phoneNumber, other.phoneNumber);
	}
}
