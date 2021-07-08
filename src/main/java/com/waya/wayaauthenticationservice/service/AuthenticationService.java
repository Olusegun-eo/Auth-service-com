package com.waya.wayaauthenticationservice.service;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.EmailPojo;
import com.waya.wayaauthenticationservice.pojo.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.PasswordPojo2;
import com.waya.wayaauthenticationservice.pojo.PinPojo;
import com.waya.wayaauthenticationservice.pojo.PinPojo2;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo2;
import com.waya.wayaauthenticationservice.pojo.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.WalletPojo;
import com.waya.wayaauthenticationservice.pojo.WayagramPojo;

public interface AuthenticationService {

	ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device);

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

	ResponseEntity<?> createProfileAccount(ProfilePojo profilePojo);

	ResponseEntity<?> createCorporateProfileAccount(ProfilePojo2 profilePojo2);

	String generateToken(Users regUser);
	
	CompletableFuture<HttpEntity<String>> postProfile(ProfilePojo profilePojo);

	void createCorporateUser(CorporateUserPojo mUser, Long id, String token);

	void createPrivateUser(Users regUser);

}
