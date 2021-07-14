package com.waya.wayaauthenticationservice.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.EmailPojo;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.WalletPojo;
import com.waya.wayaauthenticationservice.pojo.others.WayagramPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;


public interface AuthenticationService {

	ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device, boolean adminAction);

	ResponseEntity<?> createCorporateUser(CorporateUserPojo corporateUserPojo, HttpServletRequest request,
										  Device device, boolean adminAction);

	ResponseEntity<?> resendOTPPhone(String phoneNumber);

	ResponseEntity<?> resendVerificationMail(String email, String baseUrl);

	ResponseEntity<?> verifyAccountCreation(OTPPojo otpPojo);

	ResponseEntity<?> verifyPhoneUsingOTP(OTPPojo otpPojo);

	ResponseEntity<?> validateUser();

	ResponseEntity<?> verifyEmail(EmailPojo emailPojo);

	ResponseEntity<?> userByPhone(String phone);

	ResponseEntity<?> createVirtualAccount(VirtualAccountPojo virtualAccountPojo);

	ResponseEntity<?> createWalletAccount(WalletPojo walletPojo);

	ResponseEntity<?> createWayagramAccount(WayagramPojo wayagramPojo);

	ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo, String baseUrl);

	ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo, String baseUrl);

	String generateToken(Users regUser);
	
	void createCorporateUser(CorporateUserPojo mUser, Long id, String token, String baseUrl);

	void createPrivateUser(Users regUser, String baseUrl);

}
