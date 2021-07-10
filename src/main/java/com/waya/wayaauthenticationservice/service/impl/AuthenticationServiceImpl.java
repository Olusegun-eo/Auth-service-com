package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.util.Constant.CORPORATE_PROFILE_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.PROFILE_ACCOUNT_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.PROFILE_SERVICE;
import static com.waya.wayaauthenticationservice.util.Constant.VIRTUAL_ACCOUNT_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.WALLET_ACCOUNT_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.WAYAGRAM_PROFILE_TOPIC;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.CorporateUser;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.DevicePojo;
import com.waya.wayaauthenticationservice.pojo.EmailPojo;
import com.waya.wayaauthenticationservice.pojo.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.PasswordPojo2;
import com.waya.wayaauthenticationservice.pojo.PinPojo;
import com.waya.wayaauthenticationservice.pojo.PinPojo2;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo2;
import com.waya.wayaauthenticationservice.pojo.ValidateUserPojo;
import com.waya.wayaauthenticationservice.pojo.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.WalletPojo;
import com.waya.wayaauthenticationservice.pojo.WayagramPojo;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.CorporateUserRepository;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.GeneralResponse;
import com.waya.wayaauthenticationservice.response.ProfileResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RolesRepository rolesRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisUserDao redisUserDao;

	@Autowired
	private AuthenticatedUserFacade authenticatedUserFacade;

	@Autowired
	KafkaMessageProducer kafkaMessageProducer;

	@Autowired
	private CorporateUserRepository corporateUserRepository;

	@Autowired
	private WalletProxy walletProxy;

	@Autowired
	private VirtualAccountProxy virtualAccountProxy;

	@Autowired
	private ReqIPUtils reqUtil;

	@Autowired
	ProfileServiceDAO profileServiceDAO;

	@Autowired
	private ModelMapper mapper;

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
	private static final String SECRET_TOKEN = "wayas3cr3t";
	public static final String TOKEN_PREFIX = "serial ";

	@Value("${wallet.profile.url}")
	private String profileURL;

	@Override
	@Transactional
	public ResponseEntity<?> createUser(BaseUserPojo mUser, HttpServletRequest request, Device device, boolean adminAction) {
		try {
			// Check if email exists
			Users existingEmail = userRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
			if (existingEmail != null)
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);

			// Check if Phone exists
			Users existingTelephone = mUser.getPhoneNumber() == null ? null
					: userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
			if (existingTelephone != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			List<Roles> roleList = new ArrayList<>();
			Roles userRole = rolesRepo.findByName("ROLE_USER")
					.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
			roleList.add(userRole);

			if (mUser.isAdmin()) {
				Roles adminRole = rolesRepo.findByName("ROLE_ADMIN")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
				roleList.add(adminRole);
			}

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = this.reqUtil.GetDevice(device);

			Users user = new Users();
			user.setId(0L);
			user.setAdmin(mUser.isAdmin());
			user.setEmail(mUser.getEmail().trim());
			user.setFirstName(mUser.getFirstName());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname());
			user.setDateCreated(LocalDateTime.now());
			user.setRegDeviceIP(ip);
			String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
			user.setName(fullName);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			if(adminAction) user.setActive(true);
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRolesList(roleList);

			Users regUser = userRepo.saveAndFlush(user);
			if (regUser == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			createPrivateUser(regUser);
			
			return new ResponseEntity<>(new SuccessResponse(
					"User Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> createCorporateUser(CorporateUserPojo mUser, HttpServletRequest request, Device device) {

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

			Roles merchRole = rolesRepo.findByName("ROLE_MERCH")
					.orElseThrow(() -> new CustomException("Merchant Role Not Available", HttpStatus.BAD_REQUEST));

			Roles userRole = rolesRepo.findByName("ROLE_USER")
					.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

			List<Roles> roleList = new ArrayList<>();
			roleList.addAll(Arrays.asList(userRole, merchRole));
			
			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = reqUtil.GetDevice(device);

			Users user = new Users();
			user.setAdmin(mUser.isAdmin());
			user.setId(0L);
			user.setCorporate(true);
			user.setDateCreated(LocalDateTime.now());
			user.setRegDeviceIP(ip);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRolesList(roleList);
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

			Users regUser = userRepo.saveAndFlush(user);
			if (regUser == null)
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			String token = generateToken(regUser);
			createCorporateUser(mUser, regUser.getId(), token);

			return new ResponseEntity<>(new SuccessResponse(
					"Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	public void createCorporateUser(CorporateUserPojo mUser, Long userId, String token) {

		CorporateUser coopUser = mapper.map(mUser, CorporateUser.class);
		coopUser.setBusinessType(mUser.getBusinessType());
		coopUser.setUserId(userId);
		coopUser = corporateUserRepository.save(coopUser);

		CreateAccountPojo createAccount = new CreateAccountPojo();
		createAccount.setEmailAddress(coopUser.getEmail());
		createAccount.setExternalId(userId);
		createAccount.setFirstName(coopUser.getFirstName());
		createAccount.setLastName(coopUser.getSurname());
		createAccount.setMobileNo(coopUser.getPhoneNumber());
		createAccount.setSavingsProductId(1);
		walletProxy.createCorporateAccount(createAccount);

		ProfilePojo2 profilePojo = new ProfilePojo2();
		profilePojo.setBusinessType(coopUser.getBusinessType());
		profilePojo.setOrganisationEmail(coopUser.getOrgEmail());
		profilePojo.setOrganisationName(coopUser.getOrgName());
		profilePojo.setOrganisationType(coopUser.getOrgType());
		profilePojo.setReferralCode(coopUser.getReferenceCode());
		profilePojo.setEmail(coopUser.getEmail());
		profilePojo.setSurname(coopUser.getSurname());
		profilePojo.setUserId(String.valueOf(userId));
		profilePojo.setPhoneNumber(coopUser.getPhoneNumber());
		profilePojo.setFirstName(coopUser.getFirstName());
		profilePojo.setCorporate(true);

		VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
		virtualAccountPojo.setAccountName(coopUser.getFirstName() + " " + coopUser.getSurname());
		virtualAccountPojo.setUserId(String.valueOf(userId));

		ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);
		log.info("Response: {}", response.getBody());

		kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC, profilePojo);
		try {
			// Intentional delay
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} 
		Integer profileCount = profileServiceDAO.getProfileCount(String.valueOf(userId), coopUser.getPhoneNumber());
		if (profileCount == 0) {
			log.info("Profile does not exist: use an async");
			postProfile(profilePojo);
		}
	}
	
	public void createPrivateUser(Users user) {
		String id = String.valueOf(user.getId());
		
		VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
		virtualAccountPojo.setAccountName(user.getFirstName() + " " + user.getSurname());
		virtualAccountPojo.setUserId(id);

		String token = generateToken(user);
		ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);

		log.info("Response: {}", response.getBody());
		// TODO: Confirm that the Number is important for Profile Service Call to Fly
		ProfilePojo profilePojo = new ProfilePojo(user.getEmail(), user.getFirstName(), user.getPhoneNumber(),
				user.getSurname(), id, false);

		// TODO: Confirm and refactor the Kafka Call
		kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC, profilePojo);
		try {
			// Intentional delay
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} 
		Integer checkCount = profileServiceDAO.getProfileCount(id, user.getPhoneNumber());
		if (checkCount == 0) {
			log.info("Profile does not exist: use an async");
			postProfile(profilePojo);
		}
	}

	public String generateToken(Users userResponse) {
		try {
			System.out.println("::::::GENERATE TOKEN:::::");
			String token = Jwts.builder().setSubject(userResponse.getEmail())
					.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
					.signWith(SignatureAlgorithm.HS512, SECRET_TOKEN).compact();
			System.out.println(":::::Token:::::");
			return TOKEN_PREFIX + token;
		} catch (Exception e) {
			System.out.println(e.fillInStackTrace());
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

	@Override
	public ResponseEntity<?> createPin(PinPojo pinPojo) {
		try {
			// Check if email exists
			Users existingEmail = userRepo.findById(pinPojo.getUserId()).orElse(null);

			if (existingEmail != null) {
				if (!pinIs4Digit(pinPojo.getPin())) {
					return new ResponseEntity<>(new ErrorResponse("Transaction pin should be exactly 4 Digits"),
							HttpStatus.BAD_REQUEST);
				}
				existingEmail.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getPin())));
				existingEmail.setPinCreated(true);
				userRepo.save(existingEmail);
				return new ResponseEntity<>(new SuccessResponse("Transaction pin created successfully.", null),
						HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(new ErrorResponse("This email does exists"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<?> verifyAccountCreation(OTPPojo otpPojo) {
		String url = PROFILE_SERVICE + "profile-service/otp-verify/" + otpPojo.getPhoneOrEmail() + "/"
				+ otpPojo.getOtp();
		ProfileResponse profileResponse = restTemplate.getForObject(url, ProfileResponse.class);
		log.info("Error::: {}, {} and {}", new Gson().toJson(profileResponse));
		if (profileResponse.isStatus()) {
			Users user = userRepo.findByEmailOrPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
			if (user == null)
				return new ResponseEntity<>(new ErrorResponse(
						ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + "For User with " + otpPojo.getPhoneOrEmail()),
						HttpStatus.BAD_REQUEST);
			user.setActive(true);
			user.setDateOfActivation(LocalDateTime.now());
			try {
				userRepo.save(user);
				return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null),
						HttpStatus.CREATED);

			} catch (Exception e) {
				log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
				return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(new ErrorResponse(profileResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> verifyPhoneUsingOTP(OTPPojo otpPojo) {
		String url = PROFILE_SERVICE + "profile-service/otp-verify/" + otpPojo.getPhoneOrEmail() + "/"
				+ otpPojo.getOtp();
		ProfileResponse profileResponse = restTemplate.getForObject(url, ProfileResponse.class);
		log.info("Error::: {}, {} and {}", new Gson().toJson(profileResponse));
		if (profileResponse.isStatus()) {
			Users user = userRepo.findByPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
			user.setPhoneVerified(true);
			// user.setActive(true);
			try {
				userRepo.save(user);
				return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null),
						HttpStatus.CREATED);

			} catch (Exception e) {
				log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
				return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(new ErrorResponse(profileResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> verifyEmail(EmailPojo emailPojo) {
		String url = PROFILE_SERVICE + "profile-service/email-verify/" + emailPojo.getEmail() + "/"
				+ emailPojo.getToken();
		GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
		if (generalResponse.isStatus()) {
			Users user = userRepo.findByEmailIgnoreCase(emailPojo.getEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
			}
			user.setEmailVerified(true);
			user.setActive(true);
			try {
				userRepo.save(user);
				return new ResponseEntity<>(new SuccessResponse("Email verified successfully. Please login.", null),
						HttpStatus.CREATED);

			} catch (Exception e) {
				log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
				return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
			}

		} else {
			return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> changePassword(PasswordPojo passwordPojo) {
		Users user = userRepo.findByEmailIgnoreCase(passwordPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
		}
		boolean isPasswordMatched = passwordEncoder.matches(passwordPojo.getOldPassword(), user.getPassword());
		if (!isPasswordMatched) {
			return new ResponseEntity<>(new ErrorResponse("Incorrect Old Password"), HttpStatus.BAD_REQUEST);
		}
		String newPassword = passwordEncoder.encode(passwordPojo.getNewPassword());
		user.setPassword(newPassword);
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> forgotPassword(PasswordPojo2 passwordPojo) {
		Users user = userRepo.findByEmailIgnoreCase(passwordPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
		}
		user.setPassword(passwordEncoder.encode(passwordPojo.getNewPassword()));
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> changePin(PinPojo2 pinPojo) {
		Users user = userRepo.findByEmailIgnoreCase(pinPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
		}
		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pinPojo.getOldPin()), user.getPinHash());
		if (!isPinMatched) {
			return new ResponseEntity<>(new ErrorResponse("Incorrect Old Pin"), HttpStatus.BAD_REQUEST);
		}
		user.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getNewPin())));
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> forgotPin(PinPojo pinPojo) {
		Users user = userRepo.findByEmailIgnoreCase(pinPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.NOT_FOUND);
		}
		user.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getPin())));
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resendOTPPhone(String phoneNumber) {
		Users user = userRepo.findByPhoneNumber(phoneNumber).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.NOT_FOUND);

		String url = PROFILE_SERVICE + "profile-service/otp/" + phoneNumber + "/" + user.getEmail();
		GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
		if (generalResponse.isStatus()) {
			return new ResponseEntity<>(new SuccessResponse("OTP sent successfully.", null), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resendVerificationMail(String email) {
		Users user = userRepo.findByEmailIgnoreCase(email).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
					HttpStatus.NOT_FOUND);

		String url = PROFILE_SERVICE + "profile-service/email-token/" + email + "/" + user.getId();
		GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
		if (generalResponse.isStatus()) {
			return new ResponseEntity<>(new SuccessResponse("Verification email sent successfully.", null),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> validateUser() {

		Users user = authenticatedUserFacade.getUser();
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid user."), HttpStatus.OK);
		} else {
			List<String> roles = new ArrayList<>();
			Collection<Roles> userRoles = user.getRolesList();
			for (Roles r : userRoles) {
				roles.add(r.getName());
			}
			ValidateUserPojo validateUserPojo = new ValidateUserPojo();
			validateUserPojo.setCorporate(user.isCorporate());
			validateUserPojo.setEmail(user.getEmail());
			validateUserPojo.setEmailVerified(user.isEmailVerified());
			validateUserPojo.setFirstName(user.getFirstName());
			validateUserPojo.setSurname(user.getSurname());
			validateUserPojo.setPhoneVerified(user.isPhoneVerified());
			validateUserPojo.setPinCreated(user.isPinCreated());
			validateUserPojo.setId(user.getId());
			validateUserPojo.setReferenceCode(user.getReferenceCode());
			validateUserPojo.setPhoneNumber(user.getPhoneNumber());
			validateUserPojo.setRoles(roles);
			return new ResponseEntity<>(new SuccessResponse("User valid.", validateUserPojo), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> validatePin(Long userId, int pin) {
		Users users = userRepo.findById(userId).orElse(null);
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
		if (isPinMatched) {
			return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> validatePinFromUser(int pin) {
		Users users = authenticatedUserFacade.getUser();
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User."), HttpStatus.NOT_FOUND);
		}
		boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
		if (isPinMatched) {
			return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> userByPhone(String phone) {
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
	public ResponseEntity<?> createWalletAccount(WalletPojo walletPojo) {
		kafkaMessageProducer.send(WALLET_ACCOUNT_TOPIC, walletPojo);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createWayagramAccount(WayagramPojo wayagramPojo) {
		kafkaMessageProducer.send(WAYAGRAM_PROFILE_TOPIC, wayagramPojo);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createProfileAccount(ProfilePojo profilePojo) {
		kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC, profilePojo);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createCorporateProfileAccount(ProfilePojo2 profilePojo2) {
		kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC, profilePojo2);
		return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
	}

	public String startsWith234(String phoneNumber, int count) {
		return phoneNumber.substring(0, count);
	}

	public boolean pinIs4Digit(int pin) {
		String p = String.valueOf(pin);
		if (p.length() == 4) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unused")
	private void saveUserToRedis(Users user) {
		RedisUser redisUser = new RedisUser();
		redisUser.setId(user.getId());
		redisUser.setEmail(user.getEmail());
		redisUser.setPhoneNumber(user.getPhoneNumber());
		redisUser.setSurname(user.getSurname());
		redisUser.setRoles(new ArrayList<Roles>(user.getRolesList()));

		redisUserDao.save(redisUser);
	}

	@Async("asyncExecutor")
	public CompletableFuture<HttpEntity<String>> postProfile(ProfilePojo profilePojo) {
		log.info("Profile creation starts for {}", profilePojo.getEmail());
		HttpEntity<String> json = HttpRequest(profilePojo);
		ResponseEntity<String> resp = restTemplate.exchange(profileURL, HttpMethod.POST, json, String.class);
		log.info("ProfileData, {}", profilePojo);
		return CompletableFuture.completedFuture(resp);
	}

	public HttpEntity<String> HttpRequest(Object obj) {
		String jsonInString = null;
		HttpEntity<String> requestBody = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		// Request to return JSON format
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Content-Type", "application/json");
		headers.set("Cache-Control", "no-cache");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
		// Convert object to JSON string
		try {
			jsonInString = mapper.writeValueAsString(obj);
			log.info("================== :" + jsonInString);
			requestBody = new HttpEntity<>(jsonInString, headers);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return requestBody;
	}
}
