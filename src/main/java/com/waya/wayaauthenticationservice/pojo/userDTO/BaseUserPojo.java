package com.waya.wayaauthenticationservice.pojo.userDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.enums.Gender;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.EnumValue;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

import static com.waya.wayaauthenticationservice.enums.Gender.MALE;
import static com.waya.wayaauthenticationservice.util.HelperUtils.isNullOrEmpty;

@Getter
@Setter
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

	public void setEmail(String email) {
		this.email = email.replaceAll("\\s+", "").trim();
	}

	public void setPhoneNumber(String phoneNumber) {
		if(phoneNumber != null) {
			if(phoneNumber.startsWith("+"))
	        	phoneNumber = phoneNumber.substring(1);
			phoneNumber = phoneNumber.replaceAll("\\s+", "").trim();
		}
		this.phoneNumber = phoneNumber;
	}

	public void setReferenceCode(String referenceCode) {
		ReferralCode savedReferral = null;
		if (!isNullOrEmpty(referenceCode)) {
			ReferralCodeRepository refRepo =  SpringApplicationContext.getBean(ReferralCodeRepository.class);
			savedReferral = (refRepo == null) ? null :
					refRepo.getReferralCodeByCode(referenceCode).orElse(null);
		}
		this.referenceCode = (savedReferral == null) ? null : savedReferral.getReferralCode();
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName.replaceAll("\\s+", "").trim();
	}

	public void setSurname(String surname) {
		this.surname = surname.replaceAll("\\s+", "").trim();
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
