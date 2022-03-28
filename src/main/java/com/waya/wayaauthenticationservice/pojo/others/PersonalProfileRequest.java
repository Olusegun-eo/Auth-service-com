package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalProfileRequest {

    @NotBlank(message = "please provide your email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please provide your first name")
    @CustomValidator(type = Type.TEXT_STRING, message = "FirstName Passed must be Valid and not contain numerals")
    private String firstName;

    @NotBlank(message = "please provide your surname")
    @CustomValidator(type = Type.TEXT_STRING, message = "SurName Passed must be Valid and not contain numerals")
    private String surname;

    @NotBlank(message = "please provide your phone number")
    @ValidPhone
    private String phoneNumber;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth = LocalDate.now();

    private String gender;

    @NotBlank(message = "please provide the userId")
    @CustomValidator(type = Type.NUMERIC_STRING, message = "userId Passed must be Numeric")
    private String userId;

    private String referralCode;
    
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		if(phoneNumber != null) {
			if(phoneNumber.startsWith("+"))
	        	phoneNumber = phoneNumber.substring(1);
			phoneNumber = phoneNumber.replaceAll("\\s+", "").trim();
		}
		this.phoneNumber = phoneNumber;
	}
}
