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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

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
import com.waya.wayaauthenticationservice.entity.CoporateUser;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
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
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.pojo.ValidateUserPojo;
import com.waya.wayaauthenticationservice.pojo.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.WalletPojo;
import com.waya.wayaauthenticationservice.pojo.WayagramPojo;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.CooperateUserRepository;
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
	private CooperateUserRepository cooperateUserRepo;

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
	
	@Value("${app.wallet.profile.url}")
	private String profileURL;

	@Override
	@Transactional
	public ResponseEntity<?> createUser(UserPojo mUser, HttpServletRequest request, Device device) {

		try {
			// Check if email exists
			Users existingEmail = userRepo.findByEmail(mUser.getEmail()).orElse(null);
			if (existingEmail != null)
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);

			if (!mUser.getPhoneNumber().startsWith("234"))
				return new ResponseEntity<>(new ErrorResponse("Phone numbers must start with 234"),
						HttpStatus.BAD_REQUEST);

			// Check if Phone exists
			Users existingTelephone = userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
			if (existingTelephone != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			Roles mRoles = rolesRepo.findByName("ROLE_USER");
			List<Roles> roleList = new ArrayList<>();
			roleList.add(mRoles);
			if (mUser.isAdmin())
				roleList.add(rolesRepo.findByName("ROLE_ADMIN"));

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = GetDevice(device);

			Users user = this.mapper.map(mUser, Users.class);
			user.setId(0L);
			user.setAdmin(mUser.isAdmin());
			user.setDateCreated(LocalDateTime.now());
			user.setActive(true);
			user.setRegDeviceIP(ip);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setDateOfActivation(LocalDateTime.now());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setRolesList(roleList);

			Users regUser = userRepo.saveAndFlush(user);
			String id = String.valueOf(regUser.getId());

			VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
			virtualAccountPojo.setAccountName(regUser.getFirstName() + " " + regUser.getSurname());
			virtualAccountPojo.setUserId(id);

			String token = generateToken(regUser);

			ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);

			log.info("Response: {}", response.getBody());

			// Create profile by publishing to Kafka
			ProfilePojo profilePojo = new ProfilePojo(user.getEmail(), user.getFirstName(), user.getPhoneNumber(),
					user.getSurname(), String.valueOf(user.getId()), false);
			
			kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC, profilePojo);
			
			Integer checkcount = profileServiceDAO.getProfileCount(String.valueOf(user.getId()), user.getPhoneNumber());
			if(checkcount == 0) {
				log.info("Profile does not exist: use an async");
				postProfile(profilePojo);
			}

			return new ResponseEntity<>(new SuccessResponse(
					"User Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}


	@Override
	public ResponseEntity<?> createUsers(Set<UserPojo> userList, HttpServletRequest request, Device device) {
		
		
		
		return null;
	}
	
	@Override
	public ResponseEntity<?> createCorporateUser(CorporateUserPojo mUser, HttpServletRequest request, Device device) {

		try {
			// Check if email exists
			Users existingEmail = userRepo.findByEmail(mUser.getEmail()).orElse(null);
			if (existingEmail != null) {
				// String token = generateToken(existingEmail);
				// System.out.println("::::::mtoken::::" + token);
				return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);
			}

			if (!mUser.getPhoneNumber().startsWith("234"))
				return new ResponseEntity<>(new ErrorResponse("Phone numbers must start with 234"),
						HttpStatus.BAD_REQUEST);

			// Check if Phone exists
			Users existingTelephone = userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
			if (existingTelephone != null)
				return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"),
						HttpStatus.BAD_REQUEST);

			Roles merchRole = rolesRepo.findByName("ROLE_MERCH");
			if (merchRole == null)
				return new ResponseEntity<>(new ErrorResponse("Merchant Role Not Available"), HttpStatus.BAD_REQUEST);

			Roles userRole = rolesRepo.findByName("ROLE_USER");
			List<Roles> roleList = new ArrayList<>();
			roleList.addAll(Arrays.asList(userRole, merchRole));

			final String ip = reqUtil.getClientIP(request);
			log.info("Request IP: " + ip);

			DevicePojo dev = GetDevice(device);

			Users user = new Users();
			user.setId(0L);
			user.setCorporate(true);
			user.setDateCreated(LocalDateTime.now());
			user.setRegDeviceIP(ip);
			user.setRegDevicePlatform(dev.getPlatform());
			user.setRegDeviceType(dev.getDeviceType());
			user.setPassword(passwordEncoder.encode(mUser.getPassword()));
			user.setDateOfActivation(LocalDateTime.now());
			user.setRolesList(roleList);
			user.setEmail(mUser.getEmail());
			user.setEmailVerified(false);
			user.setFirstName(mUser.getFirstName());
			user.setPhoneNumber(mUser.getPhoneNumber());
			user.setPhoneVerified(false);
			user.setPinCreated(false);
			user.setReferenceCode(mUser.getReferenceCode());
			user.setSurname(mUser.getSurname());

			Users regUser = userRepo.save(user);
			if (regUser == null)
				return new ResponseEntity<>(new ErrorResponse("iD PROVIDED NOT FOUND"), HttpStatus.NOT_FOUND);

			mUser.setUserId(regUser.getId());

			CoporateUser coopUser = mapper.map(mUser, CoporateUser.class);
			coopUser.setUserId(regUser.getId());
			cooperateUserRepo.save(coopUser);

			CreateAccountPojo createAccount = new CreateAccountPojo();
			createAccount.setEmailAddress(regUser.getEmail());
			createAccount.setExternalId(regUser.getId());
			createAccount.setFirstName(regUser.getFirstName());
			createAccount.setLastName(regUser.getSurname());
			createAccount.setMobileNo(regUser.getPhoneNumber());
			createAccount.setSavingsProductId(1);
			walletProxy.createCooperateAccouont(createAccount);

			ProfilePojo2 profilePojo = new ProfilePojo2();
			profilePojo.setBusinessType(mUser.getBusinessType());
			profilePojo.setOrganisationEmail(mUser.getOrgEmail());
			profilePojo.setOrganisationName(mUser.getOrgName());
			profilePojo.setOrganisationType(mUser.getOrgType());
			profilePojo.setReferralCode(user.getReferenceCode());
			profilePojo.setEmail(user.getEmail());
			profilePojo.setSurname(user.getSurname());
			profilePojo.setUserId(String.valueOf(mUser.getUserId()));
			profilePojo.setPhoneNumber(user.getPhoneNumber());
			profilePojo.setFirstName(user.getFirstName());
			profilePojo.setCorporate(true);

			String id = String.valueOf(regUser.getId());
			VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
			virtualAccountPojo.setAccountName(regUser.getFirstName() + " " + regUser.getSurname());
			virtualAccountPojo.setUserId(id);
			String token = generateToken(regUser);

			ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);

			log.info("Response: {}", response.getBody());

			kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC, profilePojo);
			
			Integer checkcount = profileServiceDAO.getProfileCount(String.valueOf(user.getId()), user.getPhoneNumber());
			if(checkcount == 0) {
				log.info("Profile does not exist: use an async");
				postProfile(profilePojo);
			}

			return new ResponseEntity<>(new SuccessResponse(
					"Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
					HttpStatus.CREATED);

		} catch (Exception e) {
			log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
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
				String token = generateToken(existingEmail);
				System.out.println("::::::mtoken::::" + token);
//              Users user = authenticatedUserFacade.getUser();
				if (!pinIs4Digit(pinPojo.getPin())) {
					return new ResponseEntity<>(new ErrorResponse("Transaction pin should be exactly 4 Digits"),
							HttpStatus.BAD_REQUEST);
				}
				existingEmail.setPin(pinPojo.getPin());
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
	public ResponseEntity<?> verifyOTP(OTPPojo otpPojo) {
		String url = PROFILE_SERVICE + "profile-service/otp-verify/" + otpPojo.getPhone() + "/" + otpPojo.getOtp();
		ProfileResponse profileResponse = restTemplate.getForObject(url, ProfileResponse.class);
		log.info("Error::: {}, {} and {}", new Gson().toJson(profileResponse));
		if (profileResponse.isStatus()) {
			Users user = userRepo.findByPhoneNumber(otpPojo.getPhone()).orElse(null);
			user.setPhoneVerified(true);
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
			Users user = userRepo.findByEmail(emailPojo.getEmail()).orElse(null);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
			}
			user.setEmailVerified(true);
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
		Users user = userRepo.findByEmail(passwordPojo.getEmail()).orElse(null);
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
		Users user = userRepo.findByEmail(passwordPojo.getEmail()).orElse(null);
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
		Users user = userRepo.findByEmail(pinPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
		}
		boolean isPinMatched = user.getPin() == pinPojo.getOldPin();
		if (!isPinMatched) {
			return new ResponseEntity<>(new ErrorResponse("Incorrect Old Pin"), HttpStatus.BAD_REQUEST);
		}
		user.setPin(pinPojo.getNewPin());
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> forgotPin(PinPojo pinPojo) {
		Users user = userRepo.findByEmail(pinPojo.getEmail()).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
		}
		user.setPin(pinPojo.getPin());
		try {
			userRepo.save(user);
			return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resendOTP(String phoneNumber, String email) {
		String url = PROFILE_SERVICE + "profile-service/otp/" + phoneNumber + "/" + email;
		GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
		if (generalResponse.isStatus()) {
			return new ResponseEntity<>(new SuccessResponse("OTP sent successfully.", null), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> resendVerificationMail(String email, String userName) {
		String url = PROFILE_SERVICE + "profile-service/email-token/" + email + "/" + userName;
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
		Users users = userRepo.findByIdAndPin(userId, pin);
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("User valid.", users), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> validatePinFromUser(int pin) {
		Users users = authenticatedUserFacade.getUser();
		if (users == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User."), HttpStatus.BAD_REQUEST);
		}
		if (users.getPin() == pin) {
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

	private DevicePojo GetDevice(Device device) {
		String deviceType, platform;

		if (device.isNormal()) {
			deviceType = "browser";
			// viewName = "index";
		} else if (device.isMobile()) {
			deviceType = "mobile";
			// viewName = "mobile/index";
		} else if (device.isTablet()) {
			deviceType = "tablet";
			// viewName = "tablet/index";
		} else {
			deviceType = "browser";
			// viewName = "index";
		}

		platform = device.getDevicePlatform().name();

		if (platform.equalsIgnoreCase("UNKNOWN")) {
			platform = "browser";
		}
		log.info("device: " + device);
		log.info("device type: " + deviceType);
		log.info("device platform: " + device.getDevicePlatform());

		return new DevicePojo(deviceType, platform);
	}
	
	@Async("asyncExecutor")
    public CompletableFuture<HttpEntity<String>> postProfile(ProfilePojo profilePojo) throws InterruptedException 
    {
        log.info("Profile creation starts for {}", profilePojo.getEmail());
 
        //EmployeeNames employeeNameData = restTemplate.getForObject("http://localhost:8080/name", EmployeeNames.class);
        HttpEntity<String> json = HttpRequest(profilePojo);
        ResponseEntity<String> resp = restTemplate.exchange(profileURL,HttpMethod.POST, json, String.class);
        log.info("ProfileData, {}", profilePojo);
        Thread.sleep(1000L);    //Intentional delay
        log.info("Profile creation completed");
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
