package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;


@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginDetailsPojo {

    //private boolean admin = false;
    @CustomValidator(message = "Must be either a valid email or phoneNumber", type = Type.EMAIL_OR_PHONE)
    private String emailOrPhoneNumber;
    private String password;
    
	/**
	 * @return the emailOrPhoneNumber
	 */
	public String getEmailOrPhoneNumber() {
		return emailOrPhoneNumber;
	}
	
	/**
	 * @param emailOrPhoneNumber the emailOrPhoneNumber to set
	 */
	public void setEmailOrPhoneNumber(String emailOrPhoneNumber) {
		this.emailOrPhoneNumber = emailOrPhoneNumber.replaceAll("\\s+", "").trim();
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
    
    
}
