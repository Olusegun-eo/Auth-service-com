package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.EMAIL_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.JOINT_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PHONE_VERIFICATION;
import static com.waya.wayaauthenticationservice.util.Constant.VIRTUAL_ACCOUNT_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.WAYAGRAM_PROFILE_TOPIC;
import static com.waya.wayaauthenticationservice.util.HelperUtils.generateRandomNumber;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.TOKEN_PREFIX;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getExpiration;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getSecret;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordCreateContext;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.DevicePojo;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.WayagramPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.MessagingService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	KafkaMessageProducer kafkaMessageProducer;
	@Autowired
	ProfileServiceDAO profileServiceDAO;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RolesRepository rolesRepo;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private RedisUserDao redisUserDao;
	@Autowired
	private WalletProxy walletProxy;
	@Autowired
	private VirtualAccountProxy virtualAccountProxy;
	@Autowired
	private ReqIPUtils reqUtil;
	@Autowired
	private MessagingService messagingService;
	@Autowired
	private ProfileService profileService;
	@Autowired
	private OTPTokenService otpTokenService;

	@Value("${api.server.deployed}")
	private String urlRedirect;

	private String getBaseUrl(HttpServletRequest request) {
		return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
	}

	@Override
	@Transactional
	public ResponseEntity<?> createUser(BaseUserPojo mUser, HttpServletRequest request, Device device,
			boolean adminAction) {
		try {
			// Check if email exists
			Users user = mUser.getEmail() == null ? null
					: userRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
			if (user != null)
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);

			// Check if Phone exists
			user = mUser.getPhoneNumber() == null ? null
					: userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
			if (user != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			if(mUser.getEmail() == null && mUser.getPhoneNumber() == null){
				return new ResponseEntity<>(new ErrorResponse("Both Phone number and Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			List<Role> roleList = new ArrayList<>();
			Role userRole = rolesRepo.findByName("ROLE_USER")
					.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
			roleList.add(userRole);

			if (mUser.isAdmin() || mUser.isWayaAdmin()) {
				Role adminRole = rolesRepo.findByName("ROLE_APP_ADMIN")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

				roleList.add(adminRole);
			}
			if (mUser.isWayaAdmin() && adminAction) {
				Role ownerRole = rolesRepo.findByName("ROLE_OWNER_ADMIN")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

				List<Users> usersWithOwnerRole = userRepo.findByRoleList_(ownerRole);
				if (usersWithOwnerRole.isEmpty()) {
					roleList.add(ownerRole);
				}
				Role superAdminRole = rolesRepo.findByName("ROLE_SUPER_ADMIN")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
				roleList.add(superAdminRole);
			}

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = this.reqUtil.GetDevice(device);

			user = new Users();
			user.setAdmin(mUser.isAdmin());
			user.setCorporate(false);
			user.setEmail(mUser.getEmail());
			user.setFirstName(mUser.getFirstName());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname());
			user.setDateCreated(LocalDateTime.now());
			user.setAccountStatus(1);
			user.setRegDeviceIP(ip);
			String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(fullName);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRoleList(roleList);
			if (adminAction) {
				user.setActive(true);
				user.setAccountStatus(-1);
			}
			Users regUser = userRepo.saveAndFlush(user);

			if (regUser == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			if (adminAction)
				CompletableFuture.runAsync(
						() -> sendNewPassword(mUser.getPassword(), regUser));

			String token = generateToken(regUser);
			createPrivateUser(mUser, regUser.getId(), token, getBaseUrl(request));

			return new ResponseEntity<>(new SuccessResponse(
					"User Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> createCorporateUser(CorporateUserPojo mUser, HttpServletRequest request, Device device,
			boolean adminAction) {

		try {
			// Check if email exists
			Users existingEmail = userRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
			if (existingEmail != null) {
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);
			}

			// Check if Phone exists
			Users existingTelephone = userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
			if (existingTelephone != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			if(mUser.getEmail() == null && mUser.getPhoneNumber() == null){
				return new ResponseEntity<>(new ErrorResponse("Both Phone number and Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			Role userRole = rolesRepo.findByName("ROLE_USER")
					.orElseThrow(() -> new CustomException("Merchant Role Not Available", HttpStatus.BAD_REQUEST));

			Role merchRole = rolesRepo.findByName("ROLE_CORP")
					.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

			List<Role> roleList = new ArrayList<>(Arrays.asList(userRole, merchRole));
			if (mUser.isAdmin()) {
				Role corpAdminRole = rolesRepo.findByName("ROLE_CORP_ADMIN")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
				roleList.add(corpAdminRole);
			}
			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = reqUtil.GetDevice(device);

			Users user = new Users();
			user.setAdmin(false);
			user.setId(0L);
			user.setCorporate(true);
			user.setDateCreated(LocalDateTime.now());
			user.setRegDeviceIP(ip);
			user.setAccountStatus(1);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRoleList(roleList);
			user.setEmail(mUser.getEmail().trim());
			user.setEmailVerified(false);
			user.setFirstName(mUser.getFirstName());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setPhoneVerified(false);
			user.setPinCreated(false);
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname());
			String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(fullName);
			if (adminAction) {
				user.setActive(true);
				user.setAccountStatus(-1);
			}
			Users regUser = userRepo.saveAndFlush(user);

			if (regUser == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			if (adminAction)
				CompletableFuture.runAsync(
						() -> sendNewPassword(mUser.getPassword(), regUser));

			String token = generateToken(regUser);

			createCorporateUser(mUser, regUser.getId(), token, getBaseUrl(request));

			return new ResponseEntity<>(new SuccessResponse(
					"Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public void createCorporateUser(CorporateUserPojo mUser, Long userId, String token, String baseUrl) {
		String Id = String.valueOf(userId);
		// Implementation for internal calls begin here
		CorporateProfileRequest corporateProfileRequest = new CorporateProfileRequest();
		corporateProfileRequest.setBusinessType(mUser.getBusinessType());
		corporateProfileRequest.setOrganisationEmail(mUser.getOrgEmail());
		corporateProfileRequest.setOrganisationName(mUser.getOrgName());
		corporateProfileRequest.setOrganisationType(mUser.getOrgType());
		corporateProfileRequest.setReferralCode(mUser.getReferenceCode());
		corporateProfileRequest.setEmail(mUser.getEmail());
		corporateProfileRequest.setSurname(mUser.getSurname());
		corporateProfileRequest.setUserId(Id);
		corporateProfileRequest.setPhoneNumber(mUser.getPhoneNumber());
		corporateProfileRequest.setFirstName(mUser.getFirstName());
		corporateProfileRequest.setGender(mUser.getGender());
		LocalDate dateOfBirth = mUser.getDateOfBirth() == null ? LocalDate.now() : mUser.getDateOfBirth();
		corporateProfileRequest.setDateOfBirth(dateOfBirth);

		// Implementation for internal call
		log.info("CorporateProfile account creation starts: " + corporateProfileRequest);
		ApiResponseBody<String> corporateResponse = profileService.createProfile(corporateProfileRequest, baseUrl);
		log.info("CorporateProfile account creation ends: " + corporateResponse);

		// Create External Virtual Accounts
		VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
		virtualAccountPojo.setAccountName(mUser.getFirstName() + " " + mUser.getSurname());
		virtualAccountPojo.setUserId(String.valueOf(userId));
		CompletableFuture.supplyAsync(() -> virtualAccountProxy.createVirtualAccount(virtualAccountPojo))
				.orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
					if (ex != null) {
						log.error("Error Creating Virtual Account, {}", ex.getMessage());
						return new ApiResponseBody<>("An error has occurred", false);
					}
					return res;
				}).thenAccept(p -> log.info("Response from Call to Create Corporate Virtual Account is: {}", p));

		// Create Internal Wallet Accounts and Save the AccountNumber
		CreateAccountPojo createAccount = formAccountCreationPojo(userId, mUser);
		CompletableFuture.supplyAsync(() -> {
			try {
				TimeUnit.MINUTES.sleep(1);
			} catch (InterruptedException ex) {
				log.error("Error {}", ex.getMessage());
			}
			return walletProxy.createCorporateAccount(createAccount);
		}).orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
			if (ex != null) {
				log.error("Error Creating Internal Wallets Account, {}", ex.getMessage());
				return new ApiResponseBody<>("An error has occurred", false);
			}
			return res;
		}).thenAccept(p -> log.info("Response from Call to Create Corporate Wallet is: {}", p));
	}

	public void createPrivateUser(BaseUserPojo user, Long userId, String token, String baseUrl) {
		String id = String.valueOf(userId);

		PersonalProfileRequest personalProfileRequest = new PersonalProfileRequest();
		personalProfileRequest.setEmail(user.getEmail());
		personalProfileRequest.setFirstName(user.getFirstName());
		personalProfileRequest.setPhoneNumber(user.getPhoneNumber());
		personalProfileRequest.setSurname(user.getSurname());
		personalProfileRequest.setUserId(id);
		personalProfileRequest.setGender(user.getGender());
		LocalDate dateOfBirth = user.getDateOfBirth() == null ? LocalDate.now() : user.getDateOfBirth();
		personalProfileRequest.setDateOfBirth(dateOfBirth);

		log.info("PersonalProfile account creation starts: " + personalProfileRequest);
		ApiResponseBody<String> personalResponse = profileService.createProfile(personalProfileRequest, baseUrl);
		log.info("PersonalProfile account creation ends: " + personalResponse);

		// Create External Virtual Accounts
		VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
		virtualAccountPojo.setAccountName(user.getFirstName() + " " + user.getSurname());
		virtualAccountPojo.setUserId(id);
		CompletableFuture.supplyAsync(() -> virtualAccountProxy.createVirtualAccount(virtualAccountPojo))
				.orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
					if (ex != null) {
						log.error("Error Creating Virtual Account, Message is: {}", ex.getMessage());
						return new ApiResponseBody<>("An error has occurred", false);
					}
					return res;
				}).thenAccept(p -> log.info("Response from Call to Create User Virtual Account is: {}", p));

		// Create Internal Wallet Accounts
		CreateAccountPojo createAccount = formAccountCreationPojo(userId, user);
		CompletableFuture.supplyAsync(() -> {
			try {
				TimeUnit.MINUTES.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return walletProxy.createUserAccount(createAccount);
		}).orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
			if (ex != null) {
				log.error("Error Creating Wallet Account, {}", ex.getMessage());
				return new ApiResponseBody<>("An error has occurred", false);
			}
			return res;
		}).thenAccept(p -> log.info("Response from Call to Create User Wallet is: {}", p));
	}

	public String generateToken(Users user) {
		try {
			String name = user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();
			String token = Jwts.builder().setSubject(name)
					.setExpiration(new Date(System.currentTimeMillis() + getExpiration() * 1000))
					.signWith(SignatureAlgorithm.HS512, getSecret()).compact();
			return TOKEN_PREFIX + token;
		} catch (Exception e) {
			log.error("An Error Occurred:: {}", e.getMessage());
			return "";
		}
	}

	@Override
	public ResponseEntity<?> verifyAccountCreation(OTPPojo otpPojo) {
		try {
			log.info("Verify Account Creation starts {}", otpPojo);
			Users user = userRepo.findByEmailOrPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + "For User with " + otpPojo.getPhoneOrEmail()),
						HttpStatus.BAD_REQUEST);

			if (user.isActive())
				return new ResponseEntity<>(
						new SuccessResponse("Account has been Verified already. Please login.", null),
						HttpStatus.CREATED);

			OTPVerificationResponse otpResponse = otpTokenService.verifyJointOTP(otpPojo.getPhoneOrEmail(),
					otpPojo.getOtp(), JOINT_VERIFICATION);
			String message = otpResponse.getMessage();
			if (otpResponse.isValid()) {
				user.setActive(true);
				user.setDateOfActivation(LocalDateTime.now());
				// send a welcome email
				if(user.getEmail() != null && !user.getEmail().isBlank()) {
					CompletableFuture.runAsync(() -> this.profileService.sendWelcomeEmail(user));
					user.setWelcomed(true);
				}
				userRepo.save(user);

				return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null),
						HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> verifyPhoneUsingOTP(OTPPojo otpPojo) {

		Users user = userRepo.findByPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse(
					ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User: " + otpPojo.getPhoneOrEmail()),
					HttpStatus.BAD_REQUEST);
		}
		// Implementation for internal call
		log.info("Verify Phone UsingOTP starts {}", otpPojo);
		OTPVerificationResponse profileResponse = verifyOTP(otpPojo.getPhoneOrEmail(),
				Integer.parseInt(otpPojo.getOtp()));
		log.info("Verify Phone UsingOTP ends {}", profileResponse);

		if (profileResponse.isValid()) {
			user.setPhoneVerified(true);
			// user.setActive(true);
			try {
				userRepo.save(user);
				return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null),
						HttpStatus.OK);
			} catch (Exception e) {
				log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
				return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(new ErrorResponse(profileResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> verifyEmail(OTPPojo otpPojo) {
		// Implementation for internal call
		Users user = userRepo.findByEmailIgnoreCase(otpPojo.getPhoneOrEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email Passed"), HttpStatus.BAD_REQUEST);
		}
		if (user.isEmailVerified())
			return new ResponseEntity<>(new SuccessResponse("Account and Phone been Verified already.", null),
					HttpStatus.CREATED);

		log.info("Verify Email starts {}", otpPojo);
		OTPVerificationResponse emailResponse = verifyEmail(otpPojo.getPhoneOrEmail(),
				Integer.parseInt(otpPojo.getOtp()));
		if (emailResponse != null && emailResponse.isValid()) {
			user.setEmailVerified(true);
			if(!user.isWelcomed()) {
				CompletableFuture.runAsync(() -> this.profileService.sendWelcomeEmail(user));
				user.setWelcomed(true);
			}
			userRepo.save(user);
			// user.setActive(true);
			return new ResponseEntity<>(new SuccessResponse(emailResponse.getMessage()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	private OTPVerificationResponse verifyOTP(String phoneNumber, Integer otp) {
		OTPVerificationResponse verify = otpTokenService.verifySMSOTP(phoneNumber, otp, PHONE_VERIFICATION);
		return verify;
	}

	private OTPVerificationResponse verifyEmail(String email, Integer otp) {
		return otpTokenService.verifyEmailToken(email, otp, EMAIL_VERIFICATION);
	}

	private boolean sendOTP(String phoneNumber, String fullName) {
		return otpTokenService.sendSMSOTP(phoneNumber, fullName, PHONE_VERIFICATION);
	}

	private boolean pushEMailToken(String baseUrl, Users user) {
		return otpTokenService.sendVerificationEmailToken(baseUrl, user, EMAIL_VERIFICATION);
	}

	@Override
	public ResponseEntity<?> resendOTPPhone(String phoneNumber) {
		Users user = userRepo.findByPhoneNumber(phoneNumber).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.NOT_FOUND);

		if (user.isActive() && user.isPhoneVerified())
			return new ResponseEntity<>(new SuccessResponse("Account and PhoneNumber has been Verified already.", null),
					HttpStatus.CREATED);
		// Implementation for internal call
		log.info("Resend OTPPhone starts {}", phoneNumber);
		boolean generalResponse = sendOTP(phoneNumber, user.getName());
		log.info("Response From OTPPhone for {}", generalResponse);

		if (generalResponse) {
			return new ResponseEntity<>(new SuccessResponse("OTP sent successfully.", null), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse("Error"), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resendOTPForAccountVerification(String emailOrPhoneNumber, String baseUrl) {
		if(emailOrPhoneNumber.startsWith("+"))
			emailOrPhoneNumber = emailOrPhoneNumber.substring(1);
        
		Users user = userRepo.findByEmailOrPhoneNumber(emailOrPhoneNumber).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.NOT_FOUND);

		if (user.isActive())
			return new ResponseEntity<>(new SuccessResponse("Account has been Verified already.", null),
					HttpStatus.CREATED);

		this.otpTokenService.sendAccountVerificationToken(user, JOINT_VERIFICATION, baseUrl);

		return new ResponseEntity<>(new SuccessResponse("OTP sent successfully.", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> resendVerificationMail(String email, String baseUrl) {
		try {
			Users user = userRepo.findByEmailIgnoreCase(email).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
						HttpStatus.NOT_FOUND);

			// Implementation for internal call
			log.info("Resend Verification Mail starts for {}", email);
			boolean check = pushEMailToken(baseUrl, user);
			log.info("Response From Verification Mail {}", check);

			if (check) {
				return new ResponseEntity<>(new SuccessResponse("Verification email sent successfully.", null),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new ErrorResponse("Error"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> userByPhone(String phone) {
		if(phone.startsWith("+") || phone.startsWith("0"))
			phone = phone.substring(1);
        
		Users users = userRepo.findByPhoneNumber(phone).orElse(null);
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Phone Number."), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("User valid.", users), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createVirtualAccount(VirtualAccountPojo virtualAccountPojo) {
		kafkaMessageProducer.send(VIRTUAL_ACCOUNT_TOPIC, virtualAccountPojo);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createWayagramAccount(WayagramPojo wayagramPojo) {
		kafkaMessageProducer.send(WAYAGRAM_PROFILE_TOPIC, wayagramPojo);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo, String baseUrl) {
		ApiResponseBody<String> response = profileService.createProfile(profilePojo, baseUrl);
		return new ResponseEntity<>(new SuccessResponse(response.getData(), null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo, String baseUrl) {
		ApiResponseBody<String> response = profileService.createProfile(profilePojo, baseUrl);
		return new ResponseEntity<>(new SuccessResponse(response.getData(), null), HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	private void saveUserToRedis(Users user) {
		RedisUser redisUser = new RedisUser();
		redisUser.setId(user.getId());
		redisUser.setEmail(user.getEmail());
		redisUser.setPhoneNumber(user.getPhoneNumber());
		redisUser.setSurname(user.getSurname());
		redisUser.setRoles(new ArrayList<>(user.getRoleList()));

		redisUserDao.save(redisUser);
	}

	@Override
	public void sendNewPassword(String randomPassword, Users user) {
		// Email Sending of new Password Here
		if(user.getEmail() != null){
			PasswordCreateContext context = new PasswordCreateContext();
			context.init(user);
			context.setPassword(randomPassword);
			this.messagingService.sendMail(context);
		}else{
			String message = String.format("An account has been created for you with password: %s." +
					" Kindly login with your phone Number and change your password", randomPassword);
			this.messagingService.sendSMS(user.getFirstName(), message, user.getPhoneNumber());
		}
	}

	private CreateAccountPojo formAccountCreationPojo(Long userId, BaseUserPojo mUser) {
		CreateAccountPojo createAccount = new CreateAccountPojo();
		// Default Debit Limit SetUp
		createAccount.setCustDebitLimit(new BigDecimal("50000.00"));
		// Default Account Expiration Date
		LocalDateTime time = LocalDateTime.of(2099, Month.DECEMBER, 30, 0, 0);
		createAccount.setCustExpIssueDate(time.toString());
		createAccount.setUserId(userId);
		createAccount.setCustIssueId(generateRandomNumber(9));
		createAccount.setFirstName(mUser.getFirstName());
		createAccount.setLastName(mUser.getSurname());
		createAccount.setEmailId(mUser.getEmail());
		createAccount.setMobileNo(mUser.getPhoneNumber());
		createAccount.setCustSex(mUser.getGender().substring(0, 1));
		String custTitle = mUser.getGender().equals("MALE") ? "MR" : "MRS";
		createAccount.setCustTitleCode(custTitle);
		LocalDate dateOfBirth = mUser.getDateOfBirth() == null ? LocalDate.now() : mUser.getDateOfBirth();
		createAccount.setDob(dateOfBirth.toString());
		// Default Branch SOL ID
		createAccount.setSolId("0000");

		return createAccount;
	}

}
