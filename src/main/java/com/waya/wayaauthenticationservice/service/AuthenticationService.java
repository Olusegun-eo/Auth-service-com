package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import javax.servlet.http.HttpServletRequest;


public interface AuthenticationService {

	ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device, boolean adminAction);

	ResponseEntity<?> createCorporateUser(CorporateUserPojo corporateUserPojo, HttpServletRequest request,
										  Device device, boolean adminAction);

	ResponseEntity<?> resendOTPPhone(String phoneNumber);

	ResponseEntity<?> resendOTPForAccountVerification(String emailOrPhoneNumber, String baseUrl);

	ResponseEntity<?> resendVerificationMail(String email, String baseUrl);

	ResponseEntity<?> verifyAccountCreation(OTPPojo otpPojo);

	ResponseEntity<?> verifyPhoneUsingOTP(OTPPojo otpPojo);

	ResponseEntity<?> verifyEmail(OTPPojo otpPojo);

	ResponseEntity<?> userByPhone(String phone);

	ResponseEntity<?> createVirtualAccount(VirtualAccountPojo virtualAccountPojo);

	ResponseEntity<?> createWayagramAccount(WayagramPojo wayagramPojo);

	ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo, String baseUrl);

	ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo, String baseUrl);

	String generateToken(Users regUser);
	
	void createCorporateUser(CorporateUserPojo mUser, Long id, String token, String baseUrl);

	void createPrivateUser(BaseUserPojo user, Long userId, String token, String baseUrl);

	void sendNewPassword(String randomPassword, Users user);
	
	LoginResponsePojo loginPasscode(LoginPasscodePojo loginRequestModel);
	
	ResponseEntity<?> PostPasscode(PasscodePojo passcode);

}
