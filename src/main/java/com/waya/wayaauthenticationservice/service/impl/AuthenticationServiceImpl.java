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

//        if (!startsWith234(mUser.getPhoneNumber(), 3)) {
//            return new ResponseEntity<>(new ErrorResponse("Phone numbers must start with 234"), HttpStatus.BAD_REQUEST);
//         }

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
            saveUserToRedis(user);

            return new ResponseEntity<>(new SuccessResponse("User created successfully. An OTP has been sent to you", null), HttpStatus.CREATED);

        } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity createPin(PinPojo pinPojo) {
        Users user = authenticatedUserFacade.getUser();
        if(!pinIs4Digit(pinPojo.getPin())){
            return new ResponseEntity<>(new ErrorResponse("Transaction pin should be exactly 4 Digits"), HttpStatus.BAD_REQUEST);
        }
        user.setPin(pinPojo.getPin());
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
        String url = "http://46.101.41.187:8080/profile-service/otp-verify/"+otpPojo.getPhone()+"/"+otpPojo.getOtp();
        OTPResponse otpResponse = restTemplate.getForObject(url, OTPResponse.class);
        if(otpResponse.getError() != null) {
            return new ResponseEntity<>(new SuccessResponse("OTP verified successfully. Please login.", null), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Invalid OTP."), HttpStatus.BAD_REQUEST);
        }
    }


    public boolean startsWith234(Long phoneNumber, int count) {
        String s = phoneNumber.toString();
        String first3 = s.substring(0, count);
        return first3 == "234";
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
        ProfileResponse profileResponse = restTemplate.postForObject("http://46.101.41.187:8080/profile-service/personal-profile", profilePojo , ProfileResponse.class);
        if(profileResponse.getError() != null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean createWallet(WalletPojo walletPojo){
        WalletResponse walletResponse = restTemplate.postForObject("http://46.101.41.187:7090/account-creation-service/api/account/createVirtualAccount", walletPojo , WalletResponse.class);
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