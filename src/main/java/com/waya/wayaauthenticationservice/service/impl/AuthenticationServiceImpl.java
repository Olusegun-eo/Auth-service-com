package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.notification.DataPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NamesPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NotificationResponsePojo;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.EMAIL_VERIFICATION;
import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PHONE_VERIFICATION;
import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;
import static com.waya.wayaauthenticationservice.util.HelperUtils.generateRandomNumber;

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
    private AuthenticatedUserFacade authenticatedUserFacade;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private WalletProxy walletProxy;
    @Autowired
    private VirtualAccountProxy virtualAccountProxy;
    @Autowired
    private ReqIPUtils reqUtil;
    @Autowired
    private NotificationProxy notificationProxy;
    //@Autowired
    //private ModelMapper mapper;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private SMSTokenService smsTokenService;
    @Autowired
    private EmailService emailService;

    @Value("${api.server.deployed}")
    private String urlRedirect;

    private String getBaseUrl(HttpServletRequest request) {
        try {
            StringBuffer url = request.getRequestURL();
            String uri = request.getRequestURI();
            int idx = (((uri != null) && (uri.length() > 0)) ? url.indexOf(uri) : url.length());
            String host = url.substring(0, idx); //base url
            idx = host.indexOf("://");
            if (idx > 0) {
                host = host.substring(idx); //remove scheme if present
                log.info("Servers Host is {}", host);
            }
        } catch (Exception ex) {
            log.error("An Error has Occurred:: {}", ex.getMessage());
        }
        return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
    }

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

            List<Role> roleList = new ArrayList<>();
            Role userRole = rolesRepo.findByName("ROLE_USER")
                    .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
            roleList.add(userRole);

            if (mUser.isAdmin()) {
                Role adminRole = rolesRepo.findByName("ROLE_APP_ADMIN")
                        .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
                roleList.add(adminRole);
            }
            if (mUser.isWayaAdmin() && adminAction) {
                Users signedInUser = authenticatedUserFacade.getUser();
                Role ownerRole = rolesRepo.findByName("ROLE_OWNER_ADMIN")
                        .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
                if (signedInUser != null && signedInUser.getRoleList().contains(ownerRole)) {
                    Role superAdminRole = rolesRepo.findByName("ROLE_SUPER_ADMIN")
                            .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
                    roleList.add(superAdminRole);
                }
            }

            final String ip = reqUtil.getClientIP(request);
            log.info("Request IP: " + ip);

            DevicePojo dev = this.reqUtil.GetDevice(device);

            Users user = new Users();
            //String publicUserId = HelperUtils.generateRandomPassword();
            //while (userRepo.existsByUserId(publicUserId)) {
            //    publicUserId = HelperUtils.generateRandomPassword();
            //}
            //user.setUserId(publicUserId);
            user.setId(0L);
            user.setAdmin(mUser.isAdmin());
            user.setEmail(mUser.getEmail().trim());
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

            Users regUser = userRepo.saveAndFlush(user);

            if (regUser == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            // As soon as User is created by Admin Send email advising new Password
            if (adminAction) {
                user.setActive(true);
                user.setAccountStatus(-1);
                CompletableFuture.runAsync(() -> sendEmailNewPassword(mUser.getPassword(), mUser.getEmail(), mUser.getFirstName()));
            }
            String token = generateToken(regUser);
            createPrivateUser(mUser, regUser.getId(), token, getBaseUrl(request));

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
    public ResponseEntity<?> createCorporateUser(CorporateUserPojo mUser, HttpServletRequest request, Device device, boolean adminAction) {

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
            //String publicUserId = HelperUtils.generateRandomPassword();
            //while (userRepo.existsByUserId(publicUserId)) {
            //    publicUserId = HelperUtils.generateRandomPassword();
            //}
            //user.setUserId(publicUserId);

            user.setAdmin(mUser.isAdmin());
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
            Users regUser = userRepo.saveAndFlush(user);

            if (regUser == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.COULD_NOT_INSERT_RECORD.getErrorMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);

            if (adminAction) {
                user.setActive(true);
                user.setAccountStatus(-1);
                CompletableFuture.runAsync(() -> sendEmailNewPassword(mUser.getPassword(), mUser.getEmail(), mUser.getFirstName()));
            }

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

//        CorporateUser coopUser = mapper.map(mUser, CorporateUser.class);
//        coopUser.setBusinessType(mUser.getBusinessType());
//        coopUser.setPassword(passwordEncoder.encode(mUser.getPassword()));
//        coopUser.setUserId(Id);
//        coopUser = corporateUserRepository.save(coopUser);

        CreateAccountPojo createAccount = new CreateAccountPojo();
        createAccount.setUserId(userId);
        // Default Debit Limit SetUp
        createAccount.setCustDebitLimit(new BigDecimal("50000.00"));
        // Default Account Expiration Date
        createAccount.setCustExpIssueDate(LocalDateTime.of(2099, Month.DECEMBER, 30, 12, 50));
        createAccount.setCustIssueId(generateRandomNumber(9));
        createAccount.setFirstName(mUser.getFirstName());
        createAccount.setLastName(mUser.getSurname());
        createAccount.setEmailId(mUser.getEmail());
        createAccount.setMobileNo(mUser.getPhoneNumber());
        createAccount.setCustSex(mUser.getGender().substring(0, 1));
        String custTitle = mUser.getGender().equals("MALE") ? "MR" : "MRS";
        createAccount.setCustTitleCode(custTitle);
        // Default Branch SOL ID
        createAccount.setSolId("0000");
        createAccount.setDob(mUser.getDateOfBirth());
        CompletableFuture.runAsync(() -> walletProxy.createCorporateAccount(createAccount))
                .thenAccept(p -> log.debug("Response from Call to Create Corporate Wallet is: {}", p));

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

        // Implementation for internal call
        log.info("CorporateProfile account creation starts: " + corporateProfileRequest);
        ApiResponse<String> corporateResponse = profileService.createProfile(corporateProfileRequest, baseUrl);
        log.info("CorporateProfile account creation ends: " + corporateResponse);

        VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
        virtualAccountPojo.setAccountName(mUser.getFirstName() + " " + mUser.getSurname());
        virtualAccountPojo.setUserId(String.valueOf(userId));

        CompletableFuture.runAsync(() -> virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token));
    }

    public void createPrivateUser(BaseUserPojo user, Long userId, String token, String baseUrl) {
        String id = String.valueOf(userId);

        VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
        virtualAccountPojo.setAccountName(user.getFirstName() + " " + user.getSurname());
        virtualAccountPojo.setUserId(id);

        CompletableFuture.runAsync(() -> virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token));

        CreateAccountPojo createAccount = new CreateAccountPojo();
        createAccount.setUserId(userId);
        // Default Debit Limit SetUp
        createAccount.setCustDebitLimit(new BigDecimal("50000.00"));
        // Default Account Expiration Date
        createAccount.setCustExpIssueDate(LocalDateTime.of(2099, Month.DECEMBER, 30, 12, 50));
        createAccount.setCustIssueId(generateRandomNumber(9));
        createAccount.setFirstName(user.getFirstName());
        createAccount.setLastName(user.getSurname());
        createAccount.setEmailId(user.getEmail());
        createAccount.setMobileNo(user.getPhoneNumber());
        createAccount.setCustSex(user.getGender().substring(0, 1));
        String custTitle = user.getGender().equals("MALE") ? "MR" : "MRS";
        createAccount.setCustTitleCode(custTitle);
        // Default Branch SOL ID
        createAccount.setSolId("0000");
        createAccount.setDob(user.getDateOfBirth());
        CompletableFuture.runAsync(() -> walletProxy.createUserAccount(createAccount))
                .thenAccept(p -> log.debug("Response from Call to Create Private User Wallet is: {}", p));

        PersonalProfileRequest personalProfileRequest = new PersonalProfileRequest();
        personalProfileRequest.setEmail(user.getEmail());
        personalProfileRequest.setFirstName(user.getFirstName());
        personalProfileRequest.setPhoneNumber(user.getPhoneNumber());
        personalProfileRequest.setSurname(user.getSurname());
        personalProfileRequest.setUserId(id);

        log.info("PersonalProfile account creation starts: " + personalProfileRequest);
        ApiResponse<String> personalResponse = profileService.createProfile(personalProfileRequest, baseUrl);
        log.info("PersonalProfile account creation ends: " + personalResponse);
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
            log.error("An Error Occurred:: {}", e.getMessage());
            throw new RuntimeException(e.fillInStackTrace());
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
                return new ResponseEntity<>(new SuccessResponse("Account has been Verified already. Please login.", null),
                        HttpStatus.CREATED);

            Matcher matcher = emailPattern.matcher(otpPojo.getPhoneOrEmail());
            boolean isEmail = matcher.matches();

            OTPVerificationResponse otpResponse;
            if (isEmail) {
                otpResponse = verifyEmail(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
            } else {
                otpResponse = verifyOTP(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
            }
            String message = otpResponse.getMessage();
            boolean success = otpResponse.isValid();

            if (success) {
                user.setActive(true);
                user.setDateOfActivation(LocalDateTime.now());
                userRepo.save(user);

                //send a welcome email
                CompletableFuture.runAsync(() -> this.profileService.sendWelcomeEmail(user.getEmail()));

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
            return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND
                    .getErrorMessage() + " For User: " + otpPojo.getPhoneOrEmail()), HttpStatus.BAD_REQUEST);
        }
        // Implementation for internal call
        log.info("Verify Phone UsingOTP starts {}", otpPojo);
        OTPVerificationResponse profileResponse = verifyOTP(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
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
        if (user.isActive() && user.isEmailVerified())
            return new ResponseEntity<>(new SuccessResponse("Account and Phone been Verified already.", null),
                    HttpStatus.CREATED);

        log.info("Verify Email starts {}", otpPojo);
        OTPVerificationResponse emailResponse = verifyEmail(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
        if (emailResponse != null && emailResponse.isValid()) {
            user.setEmailVerified(true);
            userRepo.save(user);
            //user.setActive(true);
            return new ResponseEntity<>(new SuccessResponse(emailResponse.getMessage()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private OTPVerificationResponse verifyOTP(String phoneNumber, Integer otp) {
        OTPVerificationResponse verify = smsTokenService.verifySMSOTP(phoneNumber, otp, PHONE_VERIFICATION);
        return verify;
    }

    private OTPVerificationResponse verifyEmail(String email, Integer otp) {
        return emailService.verifyEmailToken(email, otp, EMAIL_VERIFICATION);
    }

    private boolean sendOTP(String phoneNumber, String fullName) {
        return smsTokenService.sendSMSOTP(phoneNumber, fullName, PHONE_VERIFICATION);
    }

    private boolean pushEMailToken(String baseUrl, String email) {
        Profile profile = profileRepository.findByEmail(false, email)
                .orElseThrow(() -> new CustomException("User Profile with email: " + email + "does not exist", HttpStatus.NOT_FOUND));
        return emailService.sendAcctVerificationEmailToken(baseUrl, profile, EMAIL_VERIFICATION);
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
    public ResponseEntity<?> resendVerificationMail(String email, String baseUrl) {
        try {
            Users user = userRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
                        HttpStatus.NOT_FOUND);

            // Implementation for internal call
            log.info("Resend Verification Mail starts for {}", email);
            boolean check = pushEMailToken(baseUrl, email);
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
    public ResponseEntity<?> validateUser() {
        Users user = authenticatedUserFacade.getUser();
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid user."), HttpStatus.OK);
        } else {
            Set<String> roles = new HashSet<>();
            Collection<Role> userRoles = user.getRoleList();
            Set<String> permits = new HashSet<>();
            for (Role r : userRoles) {
                roles.add(r.getName());
                permits.addAll(r.getPrivileges().stream().map(p -> p.getName()).collect(Collectors.toSet()));
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
            validateUserPojo.setPermits(permits);

            return new ResponseEntity<>(new SuccessResponse("User valid.", validateUserPojo), HttpStatus.OK);
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
    public ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo, String baseUrl) {
        ApiResponse<String> response = profileService.createProfile(profilePojo, baseUrl);
        //kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC, profilePojo);
        return new ResponseEntity<>(new SuccessResponse(response.getData(), null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo, String baseUrl) {
        ApiResponse<String> response = profileService.createProfile(profilePojo, baseUrl);
        //kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC, profilePojo2);
        return new ResponseEntity<>(new SuccessResponse(response.getData(), null), HttpStatus.OK);
    }

    public boolean pinIs4Digit(int pin) {
        String p = String.valueOf(pin);
        return p.length() == 4;
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
    public void sendEmailNewPassword(String randomPassword, String email, String firstName) {
        //Email Sending of new Password Here
        NotificationResponsePojo notification = new NotificationResponsePojo();
        NamesPojo name = new NamesPojo();
        name.setEmail(email);
        name.setFullName(firstName);
        List<NamesPojo> names = new ArrayList<>();
        names.add(name);
        DataPojo dataPojo = new DataPojo();
        String message = String.format("<h3>Hello %s </h3><br> <p> Kindly Use the password below to login to the System, " +
                        "ensure you change it.</p> <br> <h4 style=\"font-weight:bold\"> %s </h4>",
                firstName, randomPassword);
        dataPojo.setMessage(message);
        dataPojo.setNames(names);
        notification.setData(dataPojo);
        notification.setEventType("EMAIL");
        notification.setInitiator(email);
        CompletableFuture.runAsync(() -> notificationProxy.sendEmail(notification));
    }

}
