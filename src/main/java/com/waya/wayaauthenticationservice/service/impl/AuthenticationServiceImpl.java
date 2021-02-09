package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
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

import static com.waya.wayaauthenticationservice.util.Constant.ACCOUNT_CREATION;
import static com.waya.wayaauthenticationservice.util.Constant.PROFILE_SERVICE;

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
            roles.setName("User");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);

            Users user = new ModelMapper().map(mUser, Users.class);
            user.setId(0L);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            userRepo.save(user);

            // Persist Profile
            if (!createProfile(user)) {
                return new ResponseEntity<>(new ErrorResponse("There was an error completing registration"), HttpStatus.BAD_REQUEST);
            }

            // Create Wallet
            WalletPojo walletPojo = new WalletPojo("Default",String.valueOf(user.getId()));
            if (!createWallet(walletPojo)) {
                return new ResponseEntity<>(new ErrorResponse("There was an error completing registration"), HttpStatus.BAD_REQUEST);
            }

            // Save User to Redis
//            saveUserToRedis(user);


            return new ResponseEntity<>(new SuccessResponse("User created successfully. An OTP has been sent to you", null), HttpStatus.CREATED);

        } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity createCorporateUser(CorporateUserPojo mUser) {
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
            roles.setName("User");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);

            Users user = new ModelMapper().map(mUser, Users.class);
            user.setId(0L);
            user.setCorporate(true);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            userRepo.save(user);

            // Persist Profile
            mUser.setUserId(user.getId());
            if (!createCorporateProfile(mUser)) {
                return new ResponseEntity<>(new ErrorResponse("There was an error completing registration"), HttpStatus.BAD_REQUEST);
            }

            // Create Wallet
            WalletPojo walletPojo = new WalletPojo("Default",String.valueOf(user.getId()));
            if (!createWallet(walletPojo)) {
                return new ResponseEntity<>(new ErrorResponse("There was an error completing registration"), HttpStatus.BAD_REQUEST);
            }

            // Save User to Redis
//            saveUserToRedis(user);


            return new ResponseEntity<>(new SuccessResponse("User created successfully. An OTP has been sent to you", null), HttpStatus.CREATED);

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
            return new ResponseEntity<>(new SuccessResponse("User valid.", user), HttpStatus.OK);
        }
    }


    public String startsWith234(String phoneNumber, int count) {

        return  phoneNumber.substring(0, count);
    }

    public boolean pinIs4Digit(int pin) {
        String p = String.valueOf(pin);
        if(p.length() == 4) {
            return true;
        } else  {return  false ;}
    }

    private boolean createProfile(Users user){
        ProfilePojo profilePojo = new ProfilePojo(
                user.getEmail(),
                user.getFirstName(),
                "+"+user.getPhoneNumber(),
                user.getSurname(),
                String.valueOf(user.getId())
        );
        ProfileResponse profileResponse = restTemplate.postForObject(PROFILE_SERVICE+"profile-service/personal-profile", profilePojo , ProfileResponse.class);
        return profileResponse.isStatus();
    }

    private boolean createCorporateProfile(CorporateUserPojo user){
        ProfilePojo2 profilePojo = new ProfilePojo2();
        profilePojo.setBusinessType(user.getBusinessType());
        profilePojo.setOrganisationEmail(user.getOrgEmail());
        profilePojo.setOrganisationName(user.getOrgName());
        profilePojo.setOrganisationType(user.getOrgType());
        profilePojo.setReferralCode(user.getReferenceCode());
        profilePojo.setEmail(user.getEmail());
        profilePojo.setSurname(user.getSurname());
        profilePojo.setUserId(String.valueOf(user.getUserId()));
        ProfileResponse profileResponse = restTemplate.postForObject(PROFILE_SERVICE+"profile-service/corporate-profile", profilePojo , ProfileResponse.class);
        return profileResponse.isStatus();
    }

    private boolean createWallet(WalletPojo walletPojo){
        WalletResponse walletResponse = restTemplate.postForObject(ACCOUNT_CREATION+"account-creation-service/api/account/createVirtualAccount", walletPojo , WalletResponse.class);
        return walletResponse.isStatus();
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
