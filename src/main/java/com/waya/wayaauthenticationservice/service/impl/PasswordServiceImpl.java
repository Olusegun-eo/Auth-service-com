package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PASSWORD_CHANGE_EMAIL;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PASSWORD_CHANGE_PHONE;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PASSWORD_RESET_EMAIL;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PASSWORD_RESET_PHONE;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_CHANGE_EMAIL;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_CHANGE_PHONE;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_CREATE_EMAIL;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_CREATE_PHONE;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_RESET_EMAIL;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_RESET_PHONE;
import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.PasswordPolicy;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordChangeEmailContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordResetContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.PinResetContext;
import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.ChangePasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.repository.PasswordPolicyRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.security.UserPrincipal;
import com.waya.wayaauthenticationservice.service.FraudService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.CryptoUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

	private final OTPTokenService OTPTokenService;
	private final UserRepository usersRepo;
	private final FraudService fraudService;
	private final MessagingService messagingService;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AuthenticatedUserFacade authenticatedUserFacade;
	private final CryptoUtils cryptoUtils;
	private final PasswordPolicyRepository passwordPolicyRepo;

	@Override
	public ResponseEntity<?> changePassword(PasswordPojo passPojo) {
		try {
			Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
						+ " For User with input: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
			}
			boolean isPasswordMatched = passwordEncoder.matches(passPojo.getOldPassword(), user.getPassword());
			if (!isPasswordMatched) {
				return new ResponseEntity<>(new ErrorResponse("Incorrect Old Password"), HttpStatus.BAD_REQUEST);
			}

			Matcher matcher = emailPattern.matcher(passPojo.getPhoneOrEmail());
			boolean isEmail = matcher.matches();
			OTPRequestType otpRequestType = isEmail ? PASSWORD_CHANGE_EMAIL : PASSWORD_CHANGE_PHONE;

			Map<String, Object> map = doValidations(passPojo.getPhoneOrEmail(), String.valueOf(passPojo.getOtp()),
					isEmail, otpRequestType);

			boolean success = Boolean.parseBoolean(map.get("success").toString());
			if (!success) {
				String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage().replace("placeholder",
						"token: " + passPojo.getOtp()) + "for: " + passPojo.getPhoneOrEmail() + ". Message is: "
						+ map.get("message").toString();
				return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
			}
			// Check repeat
			PasswordPolicy policy = passwordPolicyRepo.findByUser(user).orElse(null);
			log.info("Forget Password: " + policy.toString());
			if (policy != null) {
				log.info("Password Validation Start");
				boolean p1 = false, p2 = false, p3 = false, p4 = false, p5 = false;
				p1 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getNewPassword());
				log.info("Password Valid: " + p1);

				if (policy.getOldPassword() != null) {
					p2 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getOldPassword());
					log.info("Old Password Valid: " + p2);
				}

				if (policy.getSecondOldPassword() != null) {
					p3 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getSecondOldPassword());
					log.info("Second Password Valid: " + p3);
				}

				if (policy.getThirdOldPassword() != null) {
					p4 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getThirdOldPassword());
					log.info("Third Password Valid: " + p4);
				}

				if (policy.getFouthOldPassword() != null) {
					p5 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getFouthOldPassword());
					log.info("Fourth Password Valid: " + p5);
				}
				log.info("Password Validation end");

				if (p1 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p2 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p3 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p4 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p5 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
			}
			if (user.isCorporate()) {
				user.setEncryptedPIN(cryptoUtils.encrypt(passPojo.getNewPassword()));
			}
			String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
			user.setPassword(newPassword);
			user.setCredentialsNonExpired(true);
			user.setAccountStatus(1);
			usersRepo.save(user);
			if (policy != null) {
				int passwordCnt = policy.getPasswordCnt();
				if (passwordCnt == 0) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(1);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 1) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(2);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 2) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 3) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setFouthOldPassword(policy.getThirdOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				}
				passwordPolicyRepo.save(policy);
			}
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> sendPasswordChangeOTPByEmail(String email, String baseUrl) {
		try {
			Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with email: " + email, null),
						HttpStatus.BAD_REQUEST);

			PasswordChangeEmailContext emailContext = new PasswordChangeEmailContext();
			Integer otpToken = generateEmailOTP(email, PASSWORD_CHANGE_EMAIL);
			emailContext.init(user);
			emailContext.redirectTo(baseUrl);
			emailContext.setToken(String.valueOf(otpToken));
			// Send the Mail
			CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

			return new ResponseEntity<>(new SuccessResponse("Email for Password Reset has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPasswordChangeOTPByPhoneNumber(String phoneNumber) {
		try {
			if (phoneNumber.startsWith("+"))
				phoneNumber = phoneNumber.substring(1);
			final String number = phoneNumber;

			Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with phoneNumber: " + phoneNumber,
						null), HttpStatus.BAD_REQUEST);

			// Send the Phone Number
			CompletableFuture
					.runAsync(() -> this.OTPTokenService.sendSMSOTP(number, user.getName(), PASSWORD_CHANGE_PHONE));

			return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> resetPassword(ResetPasswordPojo passPojo) {
		try {
			Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
						+ " For User with identity: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
			}
			Matcher matcher = emailPattern.matcher(passPojo.getPhoneOrEmail());
			boolean isEmail = matcher.matches();
			OTPRequestType otpRequestType = isEmail ? PASSWORD_RESET_EMAIL : PASSWORD_RESET_PHONE;

			Map<String, Object> map = doValidations(passPojo.getPhoneOrEmail(), String.valueOf(passPojo.getOtp()),
					isEmail, otpRequestType);

			if (!Boolean.parseBoolean(map.get("success").toString())) {
				String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage().replace("placeholder",
						"token: " + passPojo.getOtp()) + "for: " + passPojo.getPhoneOrEmail() + ". Message is: "
						+ map.get("message").toString();
				return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
			}
			// Check repeat
			PasswordPolicy policy = passwordPolicyRepo.findByUser(user).orElse(null);
			log.info("Forget Password: " + policy.toString());
			if (policy != null) {
				log.info("Password Validation Start");
				boolean p1 = false, p2 = false, p3 = false, p4 = false, p5 = false;
				p1 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getNewPassword());
				log.info("Password Valid: " + p1);

				if (policy.getOldPassword() != null) {
					p2 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getOldPassword());
					log.info("Old Password Valid: " + p2);
				}

				if (policy.getSecondOldPassword() != null) {
					p3 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getSecondOldPassword());
					log.info("Second Password Valid: " + p3);
				}

				if (policy.getThirdOldPassword() != null) {
					p4 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getThirdOldPassword());
					log.info("Third Password Valid: " + p4);
				}

				if (policy.getFouthOldPassword() != null) {
					p5 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getFouthOldPassword());
					log.info("Fourth Password Valid: " + p5);
				}
				log.info("Password Validation end");

				if (p1 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p2 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p3 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p4 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p5 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
			}
			if (user.isCorporate()) {
				user.setEncryptedPIN(cryptoUtils.encrypt(passPojo.getNewPassword()));
			}
			String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
			user.setPassword(newPassword);
			user.setAccountStatus(1);
			usersRepo.save(user);
			if (policy != null) {
				int passwordCnt = policy.getPasswordCnt();
				if (passwordCnt == 0) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(1);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 1) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(2);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 2) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 3) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setFouthOldPassword(policy.getThirdOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				}
				passwordPolicyRepo.save(policy);
			}
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	private Integer generateEmailOTP(String email, OTPRequestType otpRequestType) {
		log.info("OTP REQUEST: " + email + "|" + otpRequestType.name());
		OTPBase otpBase = this.OTPTokenService.generateEmailToken(email, otpRequestType);
		log.info("OTP GENERATE: " + otpBase.getCode() + "|" + otpRequestType.name());
		return otpBase.getCode();
	}

	@Override
	public ResponseEntity<?> sendPasswordResetOTPByEmail(String email, String baseUrl) {
		try {
			Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with email: " + email, null),
						HttpStatus.BAD_REQUEST);

			PasswordResetContext emailContext = new PasswordResetContext();
			Integer otpToken = generateEmailOTP(email, PASSWORD_RESET_EMAIL);
			emailContext.init(user);
			emailContext.redirectTo(baseUrl);
			emailContext.seToken(String.valueOf(otpToken));
			// Send the Mail
			CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

			return new ResponseEntity<>(new SuccessResponse("Email for Password Reset has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPasswordResetOTPByPhoneNumber(String phoneNumber) {
		try {
			if (phoneNumber.startsWith("+"))
				phoneNumber = phoneNumber.substring(1);
			final String number = phoneNumber;

			Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with phoneNumber: " + phoneNumber,
						null), HttpStatus.BAD_REQUEST);

			// Send the Phone Number
			CompletableFuture
					.runAsync(() -> this.OTPTokenService.sendSMSOTP(number, user.getName(), PASSWORD_RESET_PHONE));

			return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPINResetOTPByPhoneNumber(String phoneNumber) {
		try {
			if (phoneNumber.startsWith("+"))
				phoneNumber = phoneNumber.substring(1);
			final String number = phoneNumber;

			Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with phoneNumber: " + phoneNumber,
						null), HttpStatus.BAD_REQUEST);

			if (!user.isPinCreated())
				return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

			// Send the Phone Number
			CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(number, user.getName(), PIN_RESET_PHONE));

			return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPinResetOTPByEmail(String email, String redirectUrl) {
		try {
			Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with email: " + email, null),
						HttpStatus.BAD_REQUEST);

			if (!user.isPinCreated())
				return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

			PinResetContext emailContext = new PinResetContext();
			Integer otpToken = generateEmailOTP(email, PIN_RESET_EMAIL);
			emailContext.init(user);
			emailContext.redirectTo(redirectUrl);
			emailContext.seToken(String.valueOf(otpToken));
			// Send the Mail
			CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

			return new ResponseEntity<>(new SuccessResponse("Email for Pin Reset has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPINChangeOTPByPhoneNumber(String phoneNumber) {
		try {
			if (phoneNumber.startsWith("+"))
				phoneNumber = phoneNumber.substring(1);
			final String number = phoneNumber;

			// Fetch Users Information
			Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with phoneNumber: " + phoneNumber,
						null), HttpStatus.BAD_REQUEST);

			if (!user.isPinCreated())
				return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

			// Send the Phone Number
			CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(number, user.getName(), PIN_CHANGE_PHONE));

			return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPinChangeOTPByEmail(String email, String redirectUrl) {
		try {
			// Fetch Users information by Email Address
			Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with email: " + email, null),
						HttpStatus.BAD_REQUEST);

			// Check if PIN has previously been created
			if (!user.isPinCreated())
				return new ResponseEntity<>(new ErrorResponse("Transaction PIN Not Setup yet"), HttpStatus.BAD_REQUEST);

			// Build Mail Context to send Email to
			PinResetContext emailContext = new PinResetContext();
			Integer otpToken = generateEmailOTP(email, PIN_CHANGE_EMAIL);
			emailContext.init(user);
			emailContext.redirectTo(redirectUrl);
			emailContext.seToken(String.valueOf(otpToken));

			// Send the Mail
			CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

			return new ResponseEntity<>(new SuccessResponse("Email for Pin Reset has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> changeForgotPIN(NewPinPojo pinPojo) {
		try {
			if (pinPojo.getOtp().isBlank())
				return new ResponseEntity<>(new ErrorResponse("Kindly pass in a Pin"), HttpStatus.BAD_REQUEST);

			Users user = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
						+ " For User with identity: " + pinPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);

			if (!user.isPinCreated())
				return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

			Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
			boolean isEmail = matcher.matches();
			OTPRequestType otpRequestType = isEmail ? PIN_RESET_EMAIL : PIN_RESET_PHONE;

			Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail,
					otpRequestType);

			if (!Boolean.parseBoolean(map.get("success").toString())) {
				String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage().replace("placeholder",
						"token: " + pinPojo.getOtp()) + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
						+ map.get("message").toString();
				return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
			}
			String newPin = passwordEncoder.encode(pinPojo.getPin());
			user.setPinHash(newPin);
			usersRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("PIN Changed.", null), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	private Map<String, Object> doValidations(String phoneOrEmail, String otp, boolean isEmail,
			OTPRequestType otpRequestType) {
		String message;
		boolean success;
		Map<String, Object> map = new HashMap<>();
		OTPVerificationResponse otpResponse;
		if (isEmail) {
			otpResponse = this.OTPTokenService.verifyEmailToken(phoneOrEmail, Integer.parseInt(otp), otpRequestType);
		} else {
			otpResponse = this.OTPTokenService.verifySMSOTP(phoneOrEmail, Integer.parseInt(otp), otpRequestType);
		}
		success = otpResponse != null && otpResponse.isValid();
		message = otpResponse != null ? otpResponse.getMessage() : "Failure";
		map.put("success", success);
		map.put("message", message);
		return map;
	}

	@Override
	public ResponseEntity<?> changePin(ChangePINPojo pinPojo) {
		Users user = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User Identifier"), HttpStatus.BAD_REQUEST);
		}
		if (!user.isPinCreated())
			return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pinPojo.getOldPin()), user.getPinHash());
		if (!isPinMatched) {
			return new ResponseEntity<>(new ErrorResponse("Incorrect Old Pin"), HttpStatus.BAD_REQUEST);
		}

		Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
		boolean isEmail = matcher.matches();
		OTPRequestType otpRequestType = isEmail ? PIN_CHANGE_EMAIL : PIN_CHANGE_PHONE;

		Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail, otpRequestType);
		if (!Boolean.parseBoolean(map.get("success").toString())) {
			String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage().replace("placeholder",
					"token: " + pinPojo.getOtp()) + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
					+ map.get("message").toString();
			return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
		}
		user.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getNewPin())));
		try {
			usersRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> createPin(NewPinPojo pinPojo) {
		try {
			// Check if email exists
			log.info("CREATE PIN: "+ pinPojo.getPin() + "|" + pinPojo.getPhoneOrEmail());
			
			if(pinPojo.getPin() == null || pinPojo.getPin().equals("")) {
				return new ResponseEntity<>(new ErrorResponse("Pin can't be empty or null"),
						HttpStatus.BAD_REQUEST);
			}
			Users users = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
			if (users != null) {
				if (users.isPinCreated())
					return new ResponseEntity<>(new ErrorResponse("Transaction pin exists already"),
							HttpStatus.BAD_REQUEST);

				Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
				boolean isEmail = matcher.matches();
				OTPRequestType otpRequestType = isEmail ? PIN_CREATE_EMAIL : PIN_CREATE_PHONE;

				Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail,
						otpRequestType);
				if (!Boolean.parseBoolean(map.get("success").toString())) {
					String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage().replace("placeholder",
							"token: " + pinPojo.getOtp()) + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
							+ map.get("message").toString();
					return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
				}
				users.setPinHash(passwordEncoder.encode(pinPojo.getPin()));
				users.setPinCreated(true);
				usersRepo.save(users);

				return new ResponseEntity<>(new SuccessResponse("Transaction pin created successfully.", null),
						HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(
						new ErrorResponse("This user does not exists: " + pinPojo.getPhoneOrEmail()),
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> validatePin(Long userId, int pin) {
		Users users = usersRepo.findById(false, userId).orElse(null);
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID Supplied."), HttpStatus.NOT_FOUND);
		}
		if (!users.isPinCreated())
			return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

		UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
		if (isPinMatched) {
			if (principal != null && principal.getUser().isPresent() && principal.getUser().get().getId() != null) {
				Long authId = principal.getUser().get().getId();
				if (authId.equals(userId)) {
					fraudService.actionOnPinValidateSuccess(users);
				}
			}
			return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
		} else {
			if (principal != null && principal.getUser().isPresent() && principal.getUser().get().getId() != null) {
				Long authId = principal.getUser().get().getId();
				if (authId.equals(userId)) {
					fraudService.actionOnInvalidPin(users);
				}
			}
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> validatePinFromUser(int pin) {
		Users users = authenticatedUserFacade.getUser();
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User Logged In."), HttpStatus.NOT_FOUND);
		}
		log.info(users.toString());
		if (!users.isPinCreated())
			return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"), HttpStatus.BAD_REQUEST);

		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
		log.info("PIN Validate: " + isPinMatched);
		if (isPinMatched) {
			fraudService.actionOnPinValidateSuccess(users);
			return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
		} else {
			fraudService.actionOnInvalidPin(users);
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> sendPinCreationOTPPhone(String phoneNumber) {
		try {
			if (phoneNumber.startsWith("+") || phoneNumber.startsWith("0"))
				phoneNumber = phoneNumber.substring(1);

			Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with phoneNumber: " + phoneNumber,
						null), HttpStatus.BAD_REQUEST);

			if (user.isPinCreated())
				return new ResponseEntity<>(
						new ErrorResponse(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage() + " For Pin", null),
						HttpStatus.BAD_REQUEST);

			// Send the Phone Number
			final String number = phoneNumber;
			CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(number, user.getName(), PIN_CREATE_PHONE));

			return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> sendPinCreationOTPEmail(String email, String redirectUrl) {
		try {
			Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with email: " + email, null),
						HttpStatus.BAD_REQUEST);

			if (user.isPinCreated())
				return new ResponseEntity<>(
						new ErrorResponse(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage() + " For Pin", null),
						HttpStatus.BAD_REQUEST);

			PinResetContext emailContext = new PinResetContext();
			Integer otpToken = generateEmailOTP(email, PIN_CREATE_EMAIL);
			emailContext.init(user);
			emailContext.redirectTo(redirectUrl);
			emailContext.seToken(String.valueOf(otpToken));
			// Send the Mail
			CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

			return new ResponseEntity<>(new SuccessResponse("Email for Pin Creation has been sent"), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An Error Occurred: {}", ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> changePassword(@Valid ChangePasswordPojo passPojo) {
		try {
			if (Objects.isNull(passPojo.getOldPassword()) || passPojo.getOldPassword().isBlank()) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage()
						+ " Old Password: can not be null or empty ", null), HttpStatus.BAD_REQUEST);
			}
			Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
						+ " For User with input: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
			}
			boolean isPasswordMatched = passwordEncoder.matches(passPojo.getOldPassword(), user.getPassword());
			if (!isPasswordMatched) {
				return new ResponseEntity<>(new ErrorResponse("Incorrect Old Password"), HttpStatus.BAD_REQUEST);
			}
			// Check repeat
			PasswordPolicy policy = passwordPolicyRepo.findByUser(user).orElse(null);
			log.info("Forget Password: " + policy.toString());
			if (policy != null) {
				log.info("Password Validation Start");
				boolean p1 = false, p2 = false, p3 = false, p4 = false, p5 = false;
				p1 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getNewPassword());
				log.info("Password Valid: " + p1);

				if (policy.getOldPassword() != null) {
					p2 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getOldPassword());
					log.info("Old Password Valid: " + p2);
				}

				if (policy.getSecondOldPassword() != null) {
					p3 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getSecondOldPassword());
					log.info("Second Password Valid: " + p3);
				}

				if (policy.getThirdOldPassword() != null) {
					p4 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getThirdOldPassword());
					log.info("Third Password Valid: " + p4);
				}

				if (policy.getFouthOldPassword() != null) {
					p5 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getFouthOldPassword());
					log.info("Fourth Password Valid: " + p5);
				}
				log.info("Password Validation end");

				if (p1 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p2 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p3 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p4 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p5 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
			}
			if (user.isCorporate()) {
				user.setEncryptedPIN(cryptoUtils.encrypt(passPojo.getNewPassword()));
			}
			String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
			user.setPassword(newPassword);
			user.setCredentialsNonExpired(true);
			user.setAccountStatus(1);
			usersRepo.save(user);
			if (policy != null) {
				int passwordCnt = policy.getPasswordCnt();
				if (passwordCnt == 0) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(1);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 1) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(2);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 2) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 3) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setFouthOldPassword(policy.getThirdOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				}
				passwordPolicyRepo.save(policy);
			}
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resetPassword(@Valid ChangePasswordPojo passPojo) {
		try {
			Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
						+ " For User with identity: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
			}
			// Check repeat
			PasswordPolicy policy = passwordPolicyRepo.findByUser(user).orElse(null);
			log.info("Forget Password: " + policy.toString());
			if (policy != null) {
				log.info("Password Validation Start");
				boolean p1 = false, p2 = false, p3 = false, p4 = false, p5 = false;
				p1 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getNewPassword());
				log.info("Password Valid: " + p1);

				if (policy.getOldPassword() != null) {
					p2 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getOldPassword());
					log.info("Old Password Valid: " + p2);
				}

				if (policy.getSecondOldPassword() != null) {
					p3 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getSecondOldPassword());
					log.info("Second Password Valid: " + p3);
				}

				if (policy.getThirdOldPassword() != null) {
					p4 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getThirdOldPassword());
					log.info("Third Password Valid: " + p4);
				}

				if (policy.getFouthOldPassword() != null) {
					p5 = passwordEncoder.matches(passPojo.getNewPassword(), policy.getFouthOldPassword());
					log.info("Fourth Password Valid: " + p5);
				}
				log.info("Password Validation end");

				if (p1 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p2 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p3 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p4 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
				if (p5 == true) {
					return new ResponseEntity<>(new ErrorResponse("Password already used"), HttpStatus.BAD_REQUEST);
				}
			}
			if (user.isCorporate()) {
				user.setEncryptedPIN(cryptoUtils.encrypt(passPojo.getNewPassword()));
			}
			String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
			user.setPassword(newPassword);
			user.setAccountStatus(1);
			usersRepo.save(user);
			if (policy != null) {
				int passwordCnt = policy.getPasswordCnt();
				if (passwordCnt == 0) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(1);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 1) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(2);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 2) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				} else if (passwordCnt == 3) {
					policy.setNewPassword(passPojo.getNewPassword());
					policy.setOldPassword(policy.getNewPassword());
					policy.setSecondOldPassword(policy.getOldPassword());
					policy.setThirdOldPassword(policy.getSecondOldPassword());
					policy.setFouthOldPassword(policy.getThirdOldPassword());
					policy.setChangePasswordDate(LocalDateTime.now());
					policy.setLchgDate(LocalDateTime.now());
					policy.setUpdatedPasswordDate(LocalDate.now());
					policy.setPasswordCnt(3);
					policy.setPasswordAge(0);
				}
				passwordPolicyRepo.save(policy);
			}
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

}
