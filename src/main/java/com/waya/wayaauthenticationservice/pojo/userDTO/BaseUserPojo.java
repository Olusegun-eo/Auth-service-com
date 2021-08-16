package com.waya.wayaauthenticationservice.pojo.userDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.waya.wayaauthenticationservice.enums.Gender;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.EnumValue;
import com.waya.wayaauthenticationservice.util.ValidPhone;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

import static com.waya.wayaauthenticationservice.enums.Gender.MALE;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseUserPojo {

	//@NotNull(message = "Email Cannot be Null")
	@CustomValidator(message = "Invalid Email", type = Type.EMAIL)
	private String email;

	//@NotBlank(message = "Phone Number Cannot be blank")
	@ValidPhone
	@CustomValidator(message = "Phone Number must be {max} characters", type = Type.SIZE, max = 13)
	private String phoneNumber;

	private String referenceCode;

	@NotNull(message = "First Name Cannot be Null")
	@CustomValidator(message = "First Name must be at least {min} characters", type = Type.SIZE, min = 2)
	@CustomValidator(message = "First Name cannot Contain Non Alphabets", type = Type.TEXT_STRING)
	private String firstName;

	@NotNull(message = "SurName Cannot be Null")
	@Size(min = 2, message = "SurName must be at least {min} characters")
	@CustomValidator(message = "SurName cannot Contain Non Alphabets", type = Type.TEXT_STRING)
	private String surname;

	@NotNull(message = "Password Cannot be null")
	@Size(min = 8, message = "Password must be at least {min} characters Long")
	private String password;

	private boolean isAdmin = false;

	@JsonIgnore
	private boolean isWayaAdmin = false;

	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth = LocalDate.now();

	@EnumValue(enumClass = Gender.class, message = "Must be either of type MALE or FEMALE")
	private String gender = MALE.name();

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
        if(phoneNumber.startsWith("+"))
        	phoneNumber = phoneNumber.substring(1);
        
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

	public boolean isWayaAdmin() {
		return isWayaAdmin;
	}

	public void setWayaAdmin(boolean wayaAdmin) {
		isWayaAdmin = wayaAdmin;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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
