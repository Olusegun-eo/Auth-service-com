package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.CorporateUser;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.CorporateUserRepository;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import com.waya.wayaauthenticationservice.util.HelperUtils;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    public static final String TOKEN_PREFIX = "serial ";
    private static final String SECRET_TOKEN = "wayas3cr3t";
    @Autowired
    KafkaMessageProducer kafkaMessageProducer;
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
    private CorporateUserRepository corporateUserRepository;
    @Autowired
    private WalletProxy walletProxy;
    @Autowired
    private VirtualAccountProxy virtualAccountProxy;
    @Autowired
    private ReqIPUtils reqUtil;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private SMSTokenService smsTokenService;
    @Autowired
    private EmailService emailService;

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
            String publicUserId = HelperUtils.generateRandomPassword();
            while (userRepo.existsByUserId(publicUserId)) {
                publicUserId = HelperUtils.generateRandomPassword();
            }
            user.setUserId(publicUserId);
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
            if (adminAction) user.setActive(true);
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);

            Users regUser = userRepo.saveAndFlush(user);

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

            List<Roles> roleList = new ArrayList<>(Arrays.asList(userRole, merchRole));

            final String ip = reqUtil.getClientIP(request);
            log.info("Request IP: " + ip);

            DevicePojo dev = reqUtil.GetDevice(device);

            Users user = new Users();
            String publicUserId = HelperUtils.generateRandomPassword();
            while (userRepo.existsByUserId(publicUserId)) {
                publicUserId = HelperUtils.generateRandomPassword();
            }
            user.setUserId(publicUserId);
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

            String token = generateToken(regUser);
            createCorporateUser(mUser, regUser.getUserId(), token);

            return new ResponseEntity<>(new SuccessResponse(
                    "Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public void createCorporateUser(CorporateUserPojo mUser, String userId, String token) {

        CorporateUser coopUser = mapper.map(mUser, CorporateUser.class);
        coopUser.setBusinessType(mUser.getBusinessType());
        coopUser.setPassword(passwordEncoder.encode(mUser.getPassword()));
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

        // Implementation for internal calls begin here
        CorporateProfileRequest corporateProfileRequest = new CorporateProfileRequest();
        corporateProfileRequest.setBusinessType(coopUser.getBusinessType());
        corporateProfileRequest.setOrganisationEmail(coopUser.getOrgEmail());
        corporateProfileRequest.setOrganisationName(coopUser.getOrgName());
        corporateProfileRequest.setOrganisationType(coopUser.getOrgType());
        corporateProfileRequest.setReferralCode(coopUser.getReferenceCode());
        corporateProfileRequest.setEmail(coopUser.getEmail());
        corporateProfileRequest.setSurname(coopUser.getSurname());
        corporateProfileRequest.setUserId(String.valueOf(userId));
        corporateProfileRequest.setPhoneNumber(coopUser.getPhoneNumber());
        corporateProfileRequest.setFirstName(coopUser.getFirstName());

        // Implementation for internal call
        log.info("CorporateProfile account creation starts: " + corporateProfileRequest);
        ApiResponse<String> corporateResponse = profileService.createProfile(corporateProfileRequest);
        log.info("CorporateProfile account creation ends: " + corporateResponse);

        VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
        virtualAccountPojo.setAccountName(coopUser.getFirstName() + " " + coopUser.getSurname());
        virtualAccountPojo.setUserId(String.valueOf(userId));

        ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);
        log.info("Response: {}", response.getBody());
    }

    public void createPrivateUser(Users user) {

        VirtualAccountPojo virtualAccountPojo = new VirtualAccountPojo();
        virtualAccountPojo.setAccountName(user.getFirstName() + " " + user.getSurname());
        virtualAccountPojo.setUserId(user.getUserId());

        String token = generateToken(user);
        ResponseEntity<String> response = virtualAccountProxy.createVirtualAccount(virtualAccountPojo, token);

        log.info("Response: {}", response.getBody());

        PersonalProfileRequest personalProfileRequest = new PersonalProfileRequest();
        personalProfileRequest.setEmail(user.getEmail());
        personalProfileRequest.setFirstName(user.getFirstName());
        personalProfileRequest.setPhoneNumber(user.getPhoneNumber());
        personalProfileRequest.setSurname(user.getSurname());
        personalProfileRequest.setUserId(user.getUserId());

        log.info("PersonalProfile account creation starts: " + personalProfileRequest);
        ApiResponse<String> personalResponse = profileService.createProfile(personalProfileRequest);
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
    public ResponseEntity<?> createPin(PinPojo pinPojo) {
        try {
            // Check if email exists
            Users existingEmail = userRepo.findByUserId(pinPojo.getUserId()).orElse(null);

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
        try {
            log.info("Verify Account Creation starts {}", otpPojo);
            Users user = userRepo.findByEmailOrPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(
                        ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + "For User with " + otpPojo.getPhoneOrEmail()),
                        HttpStatus.BAD_REQUEST);

            Matcher matcher = emailPattern.matcher(otpPojo.getPhoneOrEmail());
            boolean isEmail = matcher.matches();
            String message;
            boolean success;
            if (isEmail) {
                var val = verifyEmail(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
                success = val.isValid();
                message = val.getMessage();
            } else {
                var val = verifyOTP(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
                success = val.getData().isValid();
                message = val.getData().getMessage();
            }
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
        // Implementation for internal call
        log.info("Verify Phone UsingOTP starts {}", otpPojo);
        ApiResponse<OTPVerificationResponse> profileResponse = verifyOTP(otpPojo.getPhoneOrEmail(), Integer.parseInt(otpPojo.getOtp()));
        log.info("Verify Phone UsingOTP ends {}", profileResponse.getData());

        if (profileResponse.getData().isValid()) {
            Users user = userRepo.findByPhoneNumber(otpPojo.getPhoneOrEmail()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND
                        .getErrorMessage() + " For User: " + otpPojo.getPhoneOrEmail()), HttpStatus.BAD_REQUEST);
            }
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
            return new ResponseEntity<>(new ErrorResponse(profileResponse.getData().getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private ApiResponse<OTPVerificationResponse> verifyOTP(String phoneNumber, Integer otp) {
        ApiResponse<OTPVerificationResponse> verify = smsTokenService.verifySMSOTP(phoneNumber, otp);
        return verify;
    }

    private EmailVerificationResponse verifyEmail(String email, Integer token) {
        EmailVerificationResponse verifyEmail = emailService.verifyEmailToken(email, token);
        return verifyEmail;
    }

    private boolean sendOTP(String phoneNumber, String fullName) {
        return smsTokenService.sendSMSOTP(phoneNumber, fullName);
    }

    private boolean pushEMailToken(String email, String fullName) {
        return emailService.sendEmailToken(email, fullName);
    }

    @Override
    public ResponseEntity<?> verifyEmail(EmailPojo emailPojo) {
        // Implementation for internal call
        Users user = userRepo.findByEmailIgnoreCase(emailPojo.getEmail()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
        }
        log.info("Verify Email starts {}", emailPojo);
        EmailVerificationResponse generalResponse = verifyEmail(emailPojo.getEmail(), Integer.parseInt(emailPojo.getToken()));
        log.info("Verify Email starts {}", emailPojo);
        if (generalResponse.isValid()) {
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
    public ResponseEntity<?> resendVerificationMail(String email) {
        Users user = userRepo.findByEmailIgnoreCase(email).orElse(null);
        if (user == null)
            return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()),
                    HttpStatus.NOT_FOUND);

        // Implementation for internal call
        log.info("Resend Verification Mail starts for {}", email);
        boolean check = pushEMailToken(email, user.getName());
        log.info("Response From Verification Mail {}", check);

        if (check) {
            return new ResponseEntity<>(new SuccessResponse("Verification email sent successfully.", null),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Error"), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<?> validatePin(String userId, int pin) {
        Users users = userRepo.findByUserId(userId).orElse(null);
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
    public ResponseEntity<?> createProfileAccount(PersonalProfileRequest profilePojo) {
        ApiResponse<String> response = profileService.createProfile(profilePojo);
        //kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC, profilePojo);
        return new ResponseEntity<>(new SuccessResponse(response.getData(), null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createCorporateProfileAccount(CorporateProfileRequest profilePojo) {
        ApiResponse<String> response = profileService.createProfile(profilePojo);
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
        redisUser.setRoles(new ArrayList<>(user.getRolesList()));

        redisUserDao.save(redisUser);
    }
}
