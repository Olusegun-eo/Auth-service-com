package com.waya.wayaauthenticationservice.service.impl;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.entity.CooperateUser;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.*;
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
import com.waya.wayaauthenticationservice.security.AuthenticationFilter;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.waya.wayaauthenticationservice.util.Constant.*;

@Service
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public ResponseEntity createUser(UserPojo mUser) {
        // Check if email exists
        Users existingEmail = userRepo.findByEmail(mUser.getEmail()).orElse(null);
        if (existingEmail != null) {
            return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);
        }

        // Check if Phone exists
        Users existingTelephone = userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
        if (existingTelephone != null) {
            return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"), HttpStatus.BAD_REQUEST);
        }

        if (!startsWith234(mUser.getPhoneNumber(), 3).equals("234")) {
            return new ResponseEntity<>(new ErrorResponse("Phone numbers must start with 234"), HttpStatus.BAD_REQUEST);
         }

        try {
            Roles roles = new Roles();
            roles.setId(1);
            roles.setName("USER_LV1");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);

            Users user = new ModelMapper().map(mUser, Users.class);
            user.setId(0L);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            userRepo.save(user);

            // Create profile by publishing to Kafka
            ProfilePojo profilePojo = new ProfilePojo(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getPhoneNumber(),
                    user.getSurname(),
                    String.valueOf(user.getId()),
                    false
            );
            kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC,profilePojo);
            return new ResponseEntity<>(new SuccessResponse("User Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"), HttpStatus.CREATED);

        } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Override
    public ResponseEntity createCorporateUser(CorporateUserPojo mUser, String token) {
        // Check if email exists
        Users existingEmail = userRepo.findByEmail(mUser.getEmail()).orElse(null);
        if (existingEmail != null) {
            return new ResponseEntity<>(new ErrorResponse("This email already exists"), HttpStatus.BAD_REQUEST);
        }

        // Check if Phone exists
        Users existingTelephone = userRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
        if (existingTelephone != null) {
            return new ResponseEntity<>(new ErrorResponse("This Phone number already exists"), HttpStatus.BAD_REQUEST);
        }

        if (!startsWith234(mUser.getPhoneNumber(), 3).equals("234")) {
            return new ResponseEntity<>(new ErrorResponse("Phone numbers must start with 234"), HttpStatus.BAD_REQUEST);
        }

        try {
            Roles roles = new Roles();
            roles.setId(1);
            roles.setName("CORP_ADMIN");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);

            Users user = new ModelMapper().map(mUser, Users.class);
            user.setId(0L);
            user.setCorporate(true);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            Users regUser = userRepo.save(user);
            mUser.setUserId(user.getId());
            
            CooperateUser coopUser = new ModelMapper().map(mUser, CooperateUser.class);
            coopUser.setUserId(regUser.getId());
            cooperateUserRepo.save(coopUser);

            CreateAccountPojo createAccount = new CreateAccountPojo();
            createAccount.setEmailAddress(regUser.getEmail());
            createAccount.setExternalId(regUser.getId());
            createAccount.setFirstName(regUser.getFirstName());
            createAccount.setLastName(regUser.getSurname());
            createAccount.setMobileNo(regUser.getPhoneNumber());
            createAccount.setSavingsProductId(1);
            
            CreateAccountResponse coopAccount = walletProxy.createCooperateAccouont(createAccount, token);
            
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

            kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC,profilePojo);

            return new ResponseEntity<>(new SuccessResponse("Corporate Account Created Successfully and Sub-account creation in process. You will receive an OTP shortly for verification"), HttpStatus.CREATED);

  } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Override
    public ResponseEntity createPin(PinPojo pinPojo) {
        Users user = authenticatedUserFacade.getUser();
        if(!pinIs4Digit(pinPojo.getPin())){
            return new ResponseEntity<>(new ErrorResponse("Transaction pin should be exactly 4 Digits"), HttpStatus.BAD_REQUEST);
        }
        user.setPin(pinPojo.getPin());
        user.setPinCreated(true);
        try {
            userRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Transaction pin created successfully.", null), HttpStatus.CREATED);

        } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity verifyOTP(OTPPojo otpPojo) {
        String url = PROFILE_SERVICE+"profile-service/otp-verify/"+otpPojo.getPhone()+"/"+otpPojo.getOtp();
        ProfileResponse profileResponse = restTemplate.getForObject(url, ProfileResponse.class);
        LOGGER.info("Error::: {}, {} and {}", new Gson().toJson(profileResponse));
        if(profileResponse.isStatus()) {
            Users user = userRepo.findByPhoneNumber(otpPojo.getPhone()).orElse(null);
            user.setPhoneVerified(true);
            try {
                userRepo.save(user);
                return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null), HttpStatus.CREATED);

            } catch (Exception e) {
                LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
                return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new ErrorResponse(profileResponse.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity verifyEmail(EmailPojo emailPojo) {
        String url = PROFILE_SERVICE+"profile-service/email-verify/"+emailPojo.getEmail()+"/"+emailPojo.getToken();
        GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
        if(generalResponse.isStatus()) {
            Users user = userRepo.findByEmail(emailPojo.getEmail()).orElse(null);
            user.setEmailVerified(true);
            try {
                userRepo.save(user);
                return new ResponseEntity<>(new SuccessResponse("Email verified successfully. Please login.", null), HttpStatus.CREATED);

            } catch (Exception e) {
                LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
                return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
            }

        } else {
            return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity changePassword(PasswordPojo passwordPojo) {
        Users user = userRepo.findByEmail(passwordPojo.getEmail()).orElse(null);
        if(user == null){
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
        }
        boolean isPasswordMatched = passwordEncoder.matches(passwordPojo.getOldPassword(), user.getPassword());
        if(!isPasswordMatched) {
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
    public ResponseEntity forgotPassword(PasswordPojo2 passwordPojo) {
        Users user = userRepo.findByEmail(passwordPojo.getEmail()).orElse(null);
        if(user == null){
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
    public ResponseEntity changePin(PinPojo2 pinPojo) {
        Users user = userRepo.findByEmail(pinPojo.getEmail()).orElse(null);
        if(user == null){
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
        }
        boolean isPinMatched = user.getPin() == pinPojo.getOldPin();
        if(!isPinMatched) {
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
    public ResponseEntity forgotPin(PinPojo pinPojo) {
        Users user = userRepo.findByEmail(pinPojo.getEmail()).orElse(null);
        if(user == null){
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
    public ResponseEntity resendOTP(String phoneNumber, String email) {
        String url = PROFILE_SERVICE+"profile-service/otp/"+phoneNumber+"/"+email;
        GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
        if(generalResponse.isStatus()) {
            return new ResponseEntity<>(new SuccessResponse("OTP sent successfully.", null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity resendVerificationMail(String email, String userName) {
        String url = PROFILE_SERVICE+"profile-service/email-token/"+email+"/"+userName;
        GeneralResponse generalResponse = restTemplate.getForObject(url, GeneralResponse.class);
        if(generalResponse.isStatus()) {
            return new ResponseEntity<>(new SuccessResponse("Verification email sent successfully.", null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(generalResponse.getMessage()), HttpStatus.BAD_REQUEST);
        }    }

    @Override
    public ResponseEntity validateUser() {
        Users user = authenticatedUserFacade.getUser();
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid user."), HttpStatus.OK);
        } else {
            List<String> roles = new ArrayList<>();
            List<Roles> userRoles = user.getRolesList();
            for (Roles r: userRoles) {
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
    public ResponseEntity validatePin(Long userId, int pin) {
        Users users = userRepo.findByIdAndPin(userId, pin);
        if (users == null ){
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SuccessResponse("User valid.", users), HttpStatus.OK);
    }

    @Override
    public ResponseEntity validatePinFromUser(int pin) {
        Users users = authenticatedUserFacade.getUser();
        if (users == null ){
            return new ResponseEntity<>(new ErrorResponse("Invalid User."), HttpStatus.BAD_REQUEST);
        }
        if (users.getPin() == pin){
            return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity userByPhone(String phone) {
        Users users = userRepo.findByPhoneNumber(phone).orElse(null);
        if (users == null ){
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone Number."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SuccessResponse("User valid.", users), HttpStatus.OK);
    }

    @Override
    public ResponseEntity createVirtualAccount(VirtualAccountPojo virtualAccountPojo) {
        kafkaMessageProducer.send(VIRTUAL_ACCOUNT_TOPIC,virtualAccountPojo);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity createWalletAccount(WalletPojo walletPojo) {
        kafkaMessageProducer.send(WALLET_ACCOUNT_TOPIC,walletPojo);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);

    }

    @Override
    public ResponseEntity createWayagramAccount(WayagramPojo wayagramPojo) {
        kafkaMessageProducer.send(WAYAGRAM_PROFILE_TOPIC,wayagramPojo);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity createProfileAccount(ProfilePojo profilePojo) {
        kafkaMessageProducer.send(PROFILE_ACCOUNT_TOPIC,profilePojo);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity createCorporateProfileAccount(ProfilePojo2 profilePojo2) {
        kafkaMessageProducer.send(CORPORATE_PROFILE_TOPIC,profilePojo2);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
    }


    public String startsWith234(String phoneNumber, int count) {

        return  phoneNumber.substring(0, count);
    }

    public boolean pinIs4Digit(int pin) {
        String p = String.valueOf(pin);
        if(p.length() == 4) {
            return true;
        } else  {return  false ;
        }
    }

    private void saveUserToRedis(Users user){
        RedisUser redisUser = new RedisUser();
        redisUser.setId(user.getId());
        redisUser.setEmail(user.getEmail());
        redisUser.setPhoneNumber(user.getPhoneNumber());
        redisUser.setSurname(user.getSurname());
        redisUser.setRoles(user.getRolesList());

        redisUserDao.save(redisUser);
    }


}
