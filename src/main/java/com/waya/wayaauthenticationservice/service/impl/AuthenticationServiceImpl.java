package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.EMAIL_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.JOINT_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PHONE_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.TRANSACTION_VERIFICATION;
import static com.waya.wayaauthenticationservice.util.Constant.VIRTUAL_ACCOUNT_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.WAYAGRAM_PROFILE_TOPIC;
import static com.waya.wayaauthenticationservice.util.HelperUtils.generateRandomNumber;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.TOKEN_PREFIX;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getExpiration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.waya.wayaauthenticationservice.pojo.others.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waya.wayaauthenticationservice.entity.CorporateUser;
import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessDTO;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessPojo;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordCreateContext;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.proxy.RoleProxy;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.CorporateUserRepository;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.UserPrincipal;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.RoleService;
import com.waya.wayaauthenticationservice.util.CryptoUtils;
import com.waya.wayaauthenticationservice.util.JwtUtil;
import com.waya.wayaauthenticationservice.util.Utils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

	private final KafkaMessageProducer kafkaMessageProducer;
	private final UserRepository userRepo;
	private final RolesRepository rolesRepo;
	private final CorporateUserRepository corporateUserRepo;
	private final ReferralCodeRepository referralRepo;
	private final BCryptPasswordEncoder passwordEncoder;
	private final RedisUserDao redisUserDao;
	private final WalletProxy walletProxy;
	private final VirtualAccountProxy virtualAccountProxy;
	private final RoleProxy roleProxy;
	private final Utils reqUtil;
	private final MessagingService messagingService;
	private final ProfileService profileService;
	private final OTPTokenService otpTokenService;
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;
	private final CryptoUtils cryptoUtils;
	private final RoleService roleService;

	@Value("${api.server.deployed}")
	private String urlRedirect;

	public AuthenticationServiceImpl(KafkaMessageProducer kafkaMessageProducer, UserRepository userRepo,
									 RolesRepository rolesRepo, BCryptPasswordEncoder passwordEncoder, RedisUserDao redisUserDao,
									 WalletProxy walletProxy, VirtualAccountProxy virtualAccountProxy, Utils reqUtil,
									 MessagingService messagingService, ProfileService profileService, OTPTokenService otpTokenService,
									 RoleProxy roleProxy, JwtUtil jwtUtil, AuthenticationManager authenticationManager,
									 CorporateUserRepository corporateUserRepo, CryptoUtils cryptoUtils,RoleService roleService,
									 ReferralCodeRepository referralRepo) {
		this.kafkaMessageProducer = kafkaMessageProducer;
		this.userRepo = userRepo;
		this.rolesRepo = rolesRepo;
		this.passwordEncoder = passwordEncoder;
		this.redisUserDao = redisUserDao;
		this.walletProxy = walletProxy;
		this.virtualAccountProxy = virtualAccountProxy;
		this.reqUtil = reqUtil;
		this.messagingService = messagingService;
		this.profileService = profileService;
		this.otpTokenService = otpTokenService;
		this.roleProxy = roleProxy;
		this.jwtUtil = jwtUtil;
		this.authenticationManager = authenticationManager;
		this.corporateUserRepo = corporateUserRepo;
		this.cryptoUtils = cryptoUtils;
		this.roleService = roleService;
		this.referralRepo = referralRepo;
	}

	private static CustomException getRoleError() {
		return new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST);
	}

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

			if (mUser.getEmail() == null && mUser.getPhoneNumber() == null) {
				return new ResponseEntity<>(new ErrorResponse("Both Phone number and Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			List<Role> roleList = new ArrayList<>();
			Role userRole = rolesRepo.findByName(ERole.ROLE_USER.name())
					.orElseThrow(AuthenticationServiceImpl::getRoleError);
			roleList.add(userRole);

			if (mUser.isAdmin() || mUser.isWayaAdmin()) {
				Role adminRole = rolesRepo.findByName(ERole.ROLE_APP_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);

				roleList.add(adminRole);
			}
			if (mUser.isWayaAdmin() && adminAction) {
				Role ownerRole = rolesRepo.findByName(ERole.ROLE_OWNER_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);
				roleList.add(ownerRole);
				Role superAdminRole = rolesRepo.findByName(ERole.ROLE_SUPER_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);
				roleList.add(superAdminRole);
			}

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = this.reqUtil.GetDevice(device);

			user = new Users();
			user.setAdmin(mUser.isAdmin());
			user.setCorporate(false);
			user.setEmail(mUser.getEmail());
			user.setFirstName(mUser.getFirstName().toUpperCase());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname().toUpperCase());
			user.setAccountStatus(1);
			user.setRegDeviceIP(ip);
			String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(fullName.toUpperCase());
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRoleList(roleList);
			if (adminAction) {
				user.setActive(true);
				user.setAccountStatus(-1);
			}
			Users regUser = userRepo.saveAndFlush(user);

			if (adminAction)
				CompletableFuture.runAsync(() -> sendNewPassword(mUser.getPassword(), regUser));

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
	public ResponseEntity<?> superAdminCreateUser(SuperAdminCreatUserRequest mUser, HttpServletRequest request, Device device,
												  boolean adminAction) {
		try {

			// Check if email exists
			Users user = mUser.getEmail() == null ? null
					: userRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
			if (user != null)
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);
			// Check if Phone exists
//			user = mUser.getPhoneNumber() == null ? null
//					: userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
//			if (user != null)
//				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
//						HttpStatus.BAD_REQUEST);
			if (mUser.getEmail() == null) {
				return new ResponseEntity<>(new ErrorResponse("Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			List<Role> roleList = new ArrayList<>();
			Role userRole = rolesRepo.findByName(ERole.ROLE_USER.name())
					.orElseThrow(AuthenticationServiceImpl::getRoleError);
			roleList.add(userRole);

			if (mUser.isAdmin() || mUser.isWayaAdmin()) {
				Role adminRole = rolesRepo.findByName(ERole.ROLE_APP_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);

				roleList.add(adminRole);
			}
			if (mUser.isWayaAdmin() && adminAction) {
				Role ownerRole = rolesRepo.findByName(ERole.ROLE_OWNER_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);
				roleList.add(ownerRole);
				Role superAdminRole = rolesRepo.findByName(ERole.ROLE_SUPER_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);
				roleList.add(superAdminRole);
			}

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = this.reqUtil.GetDevice(device);

			user = new Users();
			user.setAdmin(mUser.isAdmin());
			user.setCorporate(false);
			user.setEmail(mUser.getEmail());
			user.setFirstName(mUser.getFullName().toUpperCase());
//			user.setPhoneNumber(mUser.getPhoneNumber());
//			user.setReferenceCode(mUser.getReferenceCode());
			//user.setSurname(mUser.getSurname().toUpperCase());
			user.setAccountStatus(1);
			user.setRegDeviceIP(ip);
			//String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(mUser.getFullName().toUpperCase());
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getEmail()));
			user.setRoleList(roleList);
			if (adminAction) {
				user.setActive(true);
				user.setAccountStatus(-1);
			}
			Users regUser = userRepo.saveAndFlush(user);

//			if (adminAction)
//				CompletableFuture.runAsync(() -> sendNewPassword(mUser.getPassword(), regUser));

			String token = generateToken(regUser);
			createPrivateUserBySuperAdmin(mUser, regUser.getId(), token, getBaseUrl(request));

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

			if (mUser.getEmail() == null && mUser.getPhoneNumber() == null) {
				return new ResponseEntity<>(new ErrorResponse("Both Phone number and Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			Role userRole = rolesRepo.findByName(ERole.ROLE_USER.name())
					.orElseThrow(() -> new CustomException("Merchant Role Not Available", HttpStatus.BAD_REQUEST));

			Role merchRole = rolesRepo.findByName(ERole.ROLE_CORP.name())
					.orElseThrow(AuthenticationServiceImpl::getRoleError);

			List<Role> roleList = new ArrayList<>(Arrays.asList(userRole, merchRole));
			if (mUser.isAdmin()) {
				Role corpAdminRole = rolesRepo.findByName(ERole.ROLE_CORP_ADMIN.name())
						.orElseThrow(AuthenticationServiceImpl::getRoleError);
				roleList.add(corpAdminRole);
			}
			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = reqUtil.GetDevice(device);

			Users user = new Users();
			user.setAdmin(false);
			user.setCorporate(true);
			user.setRegDeviceIP(ip);
			user.setAccountStatus(1);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRoleList(roleList);
			user.setEmail(mUser.getEmail().trim());
			user.setEmailVerified(false);
			user.setFirstName(mUser.getFirstName().toUpperCase());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setPhoneVerified(false);
			user.setPinCreated(false);
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname().toUpperCase());
			user.setEncryptedPIN(cryptoUtils.encrypt(mUser.getPassword()));
			String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(fullName.toUpperCase());
			if (adminAction) {
				user.setActive(true);
				user.setAccountStatus(-1);
			}
			Users regUser = userRepo.saveAndFlush(user);

			if (adminAction)
				CompletableFuture.runAsync(() -> sendNewPassword(mUser.getPassword(), regUser));

			String token = generateToken(regUser);

			createCorporateUser(mUser, regUser.getId(), token, getBaseUrl(request));

			return new ResponseEntity<>(new SuccessResponse(
					"Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public void createCorporateUser(CorporateUserPojo mUser, Long userId, String token, String baseUrl) {

		String Id = String.valueOf(userId);

		// Implementation for internal calls begin here
		CorporateProfileRequest profileRequest = new CorporateProfileRequest();
		profileRequest.setBusinessType(mUser.getBusinessType());
		profileRequest.setOrganisationEmail(mUser.getOrgEmail());
		profileRequest.setOrganisationName(mUser.getOrgName());
		profileRequest.setOrganisationType(mUser.getOrgType());
		profileRequest.setOrganisationPhone(mUser.getOrgPhone());
		profileRequest.setOrganizationCity(mUser.getCity());
		profileRequest.setOfficeAddress(mUser.getOfficeAddress());
		profileRequest.setOrganizationState(mUser.getState());
		profileRequest.setReferralCode(mUser.getReferenceCode());
		profileRequest.setEmail(mUser.getEmail());
		profileRequest.setSurname(mUser.getSurname());
		profileRequest.setUserId(Id);
		profileRequest.setPhoneNumber(mUser.getPhoneNumber());
		profileRequest.setFirstName(mUser.getFirstName());
		profileRequest.setGender(mUser.getGender());
		LocalDate dateOfBirth = mUser.getDateOfBirth() == null ? LocalDate.now() : mUser.getDateOfBirth();
		profileRequest.setDateOfBirth(dateOfBirth);

		// Implementation for internal call
		log.info("CorporateProfile account creation starts: " + profileRequest);
		ApiResponseBody<String> corporateResponse = profileService.createProfile(profileRequest, baseUrl);
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

		// To assign Role and Permission for corporate user
		UserAccessDTO userAccess = new UserAccessDTO();
		userAccess.setUserId(userId);
		userAccess.setName(mUser.getFirstName() + " " + mUser.getSurname());
		userAccess.setEmailAddress(mUser.getEmail());
		userAccess.setPhoneNumber(mUser.getPhoneNumber());
		userAccess.setRoleId(1L);
		String key = "WAYA855##0AUTH";
		CompletableFuture.supplyAsync(() -> roleProxy.PostUserAccess(userAccess, key)).orTimeout(3, TimeUnit.MINUTES)
				.handle((res, ex) -> {
					if (ex != null) {
						log.error("Error Creating Role Permission, {}", ex.getMessage());
						return new ApiResponseBody<>("An error has occurred", false);
					}
					return res;
				}).thenAccept(
				p -> log.info("Response from Call to Create Corporate Role and Permission Assign is: {}", p));

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
		personalProfileRequest.setReferralCode(user.getReferenceCode());
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


	public void createPrivateUserBySuperAdmin(SuperAdminCreatUserRequest user, Long userId, String token, String baseUrl) {
		String id = String.valueOf(userId);

		PersonalProfileRequest personalProfileRequest = new PersonalProfileRequest();
		personalProfileRequest.setEmail(user.getEmail());
		personalProfileRequest.setFirstName(user.getFullName());
		personalProfileRequest.setPhoneNumber("");
		personalProfileRequest.setSurname("");
		personalProfileRequest.setUserId(id);
		personalProfileRequest.setGender("");
		personalProfileRequest.setReferralCode("");
		LocalDate dateOfBirth = LocalDate.now();
				//user.getDateOfBirth() == null ? LocalDate.now() : user.getDateOfBirth();
		personalProfileRequest.setDateOfBirth(dateOfBirth);

		log.info("PersonalProfile account creation starts: " + personalProfileRequest);
		ApiResponseBody<String> personalResponse = profileService.createProfile(personalProfileRequest, baseUrl);
		log.info("PersonalProfile account creation ends: " + personalResponse);

		// Create External Virtual Accounts
		VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
		virtualAccountPojo.setAccountName(user.getFullName());
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
		BaseUserPojo user2 = new BaseUserPojo();
		user2.setPhoneNumber("2347030355111");
		user2.setEmail(user.getEmail());
		user2.setFirstName(user.getFullName());
		user2.setSurname("admin");
		user2.setDateOfBirth(dateOfBirth);
		CreateAccountPojo createAccount = formAccountCreationPojo(userId, user2);
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
			// String name = user.getEmail() != null ? user.getEmail() :
			// user.getPhoneNumber();
			/*
			 * String token = Jwts.builder().setSubject(name) .setExpiration(new
			 * Date(System.currentTimeMillis() + getExpiration() * 1000))
			 * .signWith(SignatureAlgorithm.HS512, getSecret()).compact();
			 */
			String userName = (user.getEmail() == null || user.getEmail().isBlank()) ? user.getPhoneNumber()
					: user.getEmail();
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", user.getId());
			claims.put("role", user.getRoleList());
			Date expirationDate = new Date(System.currentTimeMillis() + getExpiration());
			String token = jwtUtil.doGenerateToken(claims, userName, expirationDate);
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
				if (user.getEmail() != null && !user.getEmail().isBlank()) {
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
	public ResponseEntity<?> verifyTransactionCreation(OTPPojo otpPojo) {
		try {
			log.info("Verify Transaction Creation starts {}", otpPojo);
			Users user = userRepo.findByEmailOrPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + "For User with " + otpPojo.getPhoneOrEmail()),
						HttpStatus.BAD_REQUEST);

			OTPVerificationResponse otpResponse = otpTokenService.verifyJointOTP(otpPojo.getPhoneOrEmail(),
					otpPojo.getOtp(), TRANSACTION_VERIFICATION);
			String message = otpResponse.getMessage();
			if (otpResponse.isValid()) {
				return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Kindly proceed.", null),
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
			if (!user.isWelcomed()) {
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
		return otpTokenService.verifySMSOTP(phoneNumber, otp, PHONE_VERIFICATION);
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
		Users user = null;
		try{
			user=userRepo.findByPhoneNumber(phoneNumber).orElse(null);
		} catch (Exception exception){
			throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

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
		if (emailOrPhoneNumber.startsWith("+"))
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
	public ResponseEntity<?> resendOTPForWalletTransaction(String emailOrPhoneNumber, String baseUrl) {
		if (emailOrPhoneNumber.startsWith("+"))
			emailOrPhoneNumber = emailOrPhoneNumber.substring(1);

		Users user = userRepo.findByEmailOrPhoneNumber(emailOrPhoneNumber).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.NOT_FOUND);

		//if (user.isActive())
		//	return new ResponseEntity<>(new SuccessResponse("Account has been Verified already.", null), HttpStatus.CREATED);

		this.otpTokenService.sendAccountVerificationToken(user, TRANSACTION_VERIFICATION, baseUrl);

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
		if (phone.startsWith("+") || phone.startsWith("0"))
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
		if (user.getEmail() != null) {
			PasswordCreateContext context = new PasswordCreateContext();
			context.init(user);
			context.setPassword(randomPassword);
			this.messagingService.sendMail(context);
		} else {
			String message = String.format("An account has been created for you with password: %s."
					+ " Kindly login with your phone Number and change your password", randomPassword);
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

	@Override
	public LoginResponsePojo loginPasscode(LoginPasscodePojo login) {
		try {
			LoginResponsePojo loginResponsePojo = new LoginResponsePojo();
			CorporateUser user = login.getEmailOrPhoneNumber() == null ? null
					: corporateUserRepo.findByEmailOrPhoneNumber(login.getEmailOrPhoneNumber()).orElse(null);
			if (user == null) {
				throw new CustomException("Invalid Email OR Phone number", HttpStatus.BAD_REQUEST);
			}
			boolean passMatch = passwordEncoder.matches(login.getPasscode(), user.getPasscode());
			if (!passMatch) {
				throw new CustomException("Wrong Passcode", HttpStatus.BAD_REQUEST);
			}
			Users mUser = userRepo.findById(user.getInvitorId()).orElse(null);
			if (mUser == null) {
				throw new CustomException("No attached corporate", HttpStatus.BAD_REQUEST);
			}
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					mUser.getEmail(), cryptoUtils.decrypt(mUser.getEncryptedPIN())));
			SecurityContextHolder.getContext().setAuthentication(authentication);

			if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
				UserPrincipal userPrincipal = ((UserPrincipal) authentication.getPrincipal());
				Users sUser = userPrincipal.getUser().orElse(null);
				if (sUser == null)
					throw new CustomException("Unable authenticate", HttpStatus.BAD_REQUEST);
				UserAccessResponse access = userPrincipal.getAccess();

				String userName = (mUser.getEmail() == null || mUser.getEmail().isBlank()) ? mUser.getPhoneNumber()
						: mUser.getEmail();
				String pwd = mUser.getPassword();
				System.out.println(pwd);

				Map<String, Object> claims = new HashMap<>();
				claims.put("id", mUser.getId());
				claims.put("role", mUser.getRoleList());
				Date expirationDate = new Date(System.currentTimeMillis() + getExpiration());
				String token = jwtUtil.doGenerateToken(claims, userName, expirationDate);

				//ReferralCodeRepository referralRepo = SpringApplicationContext.getBean(ReferralCodeRepository.class);
				ReferralCode referral = referralRepo.getReferralCodeByUserId(String.valueOf(user.getId()))
						.orElse(new ReferralCode());

				Map<String, Object> m = new HashMap<>();

				Set<String> permit = getPrivileges(mUser.getRoleList());
				Set<String> roles = mUser.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());

				loginResponsePojo.setCode(0);
				loginResponsePojo.setStatus(true);
				loginResponsePojo.setMessage("Login Successful");

				if (mUser.isCorporate()) {
					// RoleService roleService =
					// SpringApplicationContext.getBean(RoleService.class);
					UserAccessPojo userAccess = null;
					//RoleService roleService = (RoleService) SpringApplicationContext.getBean("roleService");
					userAccess = roleService.getAccess(user.getInviteeId());
					if (userAccess != null) {
						m.put("corporateAccess", userAccess.getRole().getName());
					}
				}

				m.put("token", TOKEN_PREFIX + token);
				m.put("privilege", permit);
				m.put("roles", roles);
				m.put("access", access);
				m.put("pinCreated", mUser.isPinCreated());
				m.put("corporate", mUser.isCorporate());


				UserProfileResponsePojo userProfile = convert(mUser, referral);

				m.put("user", userProfile);
				loginResponsePojo.setData(m);

			}
			return loginResponsePojo;
		} catch (Exception ex) {
			log.info(ex.getMessage());
			String errorLog = String.format("%s: %s", ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage(),
					ex.getMessage());
			throw new CustomException(errorLog, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> PostPasscode(PasscodePojo passcode) {
		try {
			// Check if email exists
			CorporateUser user = passcode.getEmail() == null ? null
					: corporateUserRepo.findByEmailIgnoreCase(passcode.getEmail()).orElse(null);
			if (user != null)
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);

			// Check if Phone exists
			user = passcode.getPhoneNumber() == null ? null
					: corporateUserRepo.findByPhoneNumber(passcode.getPhoneNumber()).orElse(null);
			if (user != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			if (passcode.getEmail() == null && passcode.getPhoneNumber() == null) {
				return new ResponseEntity<>(new ErrorResponse("Both Phone number and Email cannot be null"),
						HttpStatus.BAD_REQUEST);
			}

			user = new CorporateUser();
			user.setEmail(passcode.getEmail());
			user.setPhoneNumber(passcode.getPhoneNumber());
			user.setPasscode(passwordEncoder.encode(passcode.getPasscode()));
			user.setInviteeId(passcode.getInviteeId());
			user.setInvitorId(passcode.getInvitorId());
			user.setRoleId(passcode.getRoleId());
			user.setName(passcode.getName());
			corporateUserRepo.save(user);

			return new ResponseEntity<>(new SuccessResponse("Corporate User Created Successfully "),
					HttpStatus.CREATED);

		} catch (Exception e) {
			log.error("Error::: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	private Set<String> getPrivileges(final Collection<Role> roles) {
		Set<String> privileges = new HashSet<>();
		for (Role role : roles) {
			privileges.addAll(role.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet()));
		}
		return privileges;
	}

	private UserProfileResponsePojo convert(Users user, ReferralCode referral) {
		Profile profile = referral.getProfile();
		String referralCode = referral.getReferralCode();

		Set<String> permit = getPrivileges(user.getRoleList());
		Set<String> roles = user.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());

		UserProfileResponsePojo userProfile = new UserProfileResponsePojo();

		userProfile.setId(user.getId());
		userProfile.setEmail(Objects.toString(user.getEmail(), ""));
		userProfile.setPhoneNumber(Objects.toString(user.getPhoneNumber(), ""));
		userProfile.setReferenceCode(Objects.toString(referralCode, ""));
		userProfile.setFirstName(user.getFirstName());
		userProfile.setLastName(user.getSurname());
		userProfile.setAdmin(user.isAdmin());
		userProfile.setPinCreated(user.isPinCreated());
		userProfile.setCorporate(user.isCorporate());
		userProfile.setEmailVerified(user.isEmailVerified());
		userProfile.setPhoneVerified(user.isPhoneVerified());
		userProfile.setActive(user.isActive());
		userProfile.setAccountDeleted(user.isDeleted());
		userProfile.setRoles(roles);
		userProfile.setPermits(permit);
		userProfile.setAccountExpired(!user.isAccountNonExpired());
		userProfile.setAccountLocked(!user.isAccountNonLocked());
		userProfile.setCredentialsExpired(!user.isCredentialsNonExpired());

		userProfile.setGender(Objects.toString(profile.getGender(), ""));
		userProfile.setMiddleName(Objects.toString(profile.getMiddleName(), ""));
		userProfile.setDateOfBirth(Objects.toString(profile.getDateOfBirth(), ""));
		userProfile.setDistrict(Objects.toString(profile.getDistrict(), ""));
		userProfile.setAddress(Objects.toString(profile.getAddress(), ""));
		userProfile.setCity(Objects.toString(profile.getCity(), ""));
		userProfile.setState(Objects.toString(profile.getState(), ""));
		userProfile.setProfileImage(Objects.toString(profile.getProfileImage(), ""));

		return userProfile;
	}

}