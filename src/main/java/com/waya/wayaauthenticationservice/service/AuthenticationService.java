package com.waya.wayaauthenticationservice.service;

import javax.servlet.http.HttpServletRequest;

import com.waya.wayaauthenticationservice.pojo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;

public interface AuthenticationService {

	ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device, boolean adminAction);

	ResponseEntity<?> createCorporateUser(CorporateUserPojo corporateUserPojo, HttpServletRequest request,
			Device device);

	ResponseEntity<?> createPin(PinPojo pinPojo);

	ResponseEntity<?> verifyAccountCreation(OTPPojo otpPojo);

	ResponseEntity<?> verifyPhoneUsingOTP(OTPPojo otpPojo);

	ResponseEntity<?> verifyEmail(EmailPojo emailPojo);

	ResponseEntity<?> changePassword(PasswordPojo passwordPojo);

	ResponseEntity<?> forgotPassword(PasswordPojo2 passwordPojo);

	ResponseEntity<?> changePin(PinPojo2 pinPojo);

	ResponseEntity<?> forgotPin(PinPojo pinPojo);

	ResponseEntity<?> resendOTPPhone(String phoneNumber);

	ResponseEntity<?> resendVerificationMail(String email);

	ResponseEntity<?> validateUser();

	ResponseEntity<?> validatePin(Long userId, int pin);

	ResponseEntity<?> validatePinFromUser(int pin);

	ResponseEntity<?> userByPhone(String phone);

	ResponseEntity<?> createVirtualAccount(VirtualAccountPojo virtualAccountPojo);

	ResponseEntity<?> createWalletAccount(WalletPojo walletPojo);

	ResponseEntity<?> createWayagramAccount(WayagramPojo wayagramPojo);

	ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo);

	ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo);

	String generateToken(Users regUser);
	
	//CompletableFuture<HttpEntity<String>> postProfile(ProfilePojo profilePojo);

	void createCorporateUser(CorporateUserPojo mUser, Long id, String token);

	void createPrivateUser(Users regUser);

}
