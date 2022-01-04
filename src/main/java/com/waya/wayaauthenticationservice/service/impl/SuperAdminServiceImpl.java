package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.mail.context.AccountVerificationEmailContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.PinResetContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.SuperAdminCreatUserRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.SuperAdminService;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.PIN_CHANGE_EMAIL;

@Service
@Slf4j
public class SuperAdminServiceImpl implements SuperAdminService {

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    MessagingService messagingService;


    @Override
    public ResponseEntity<?> createUser(SuperAdminCreatUserRequest userPojo, HttpServletRequest request, Device device) {
        return authenticationService.superAdminCreateUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<String> testEmailTemplate() {
        Users user = new Users();
        user.setEmail("agbe.terseer@gmail.com");
        user.setFirstName("Terseer");
        sendWelcomeEmail(user);
        return new ResponseEntity<String>("Done", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> testOTPEmailTemplate() {
        Users user = new Users();
        user.setEmail("agbe.terseer@gmail.com");
        user.setFirstName("Terseer");
        sendOTPEmail(user);
        return new ResponseEntity<String>("Done", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> testPinReset() {
        Users user = new Users();
        user.setEmail("agbe.terseer@gmail.com");
        user.setFirstName("Terseer");
        sendPinReset(user);
        return new ResponseEntity<String>("Done", HttpStatus.OK);
    }

    public void sendWelcomeEmail(Users user) {
        WelcomeEmailContext emailContext = new WelcomeEmailContext();
        emailContext.init(user);
        try {
            messagingService.sendMail(emailContext);
        } catch (Exception e) {
            log.error("An Error Occurred:: {}", e.getMessage());
        }
        log.info("Welcome email sent!! \n");
    }


    private void sendOTPEmail(Users user) {
        try {
            //generate the token
            String otp = "094824"; 
            AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
            emailContext.init(user);
            emailContext.buildURL("http://localhost.com");
            emailContext.setToken(otp);
            try {
                messagingService.sendMail(emailContext);
            } catch (Exception e) {
                log.error("An Error Occurred:: {}", e.getMessage());
            }
            // mailService.sendMail(user.getEmail(), message);
            log.info("Activation email sent!!: {} \n", user.getEmail());

        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
        }
    }




    private void sendPinReset(Users user){
        try{
        PinResetContext emailContext = new PinResetContext();
        Integer otpToken =  940349;
        emailContext.init(user);
        emailContext.redirectTo("http://localhost.com");
        emailContext.seToken(String.valueOf(otpToken));
        try {
            messagingService.sendMail(emailContext);
        } catch (Exception e) {
            log.error("An Error Occurred:: {}", e.getMessage());
        }
        // mailService.sendMail(user.getEmail(), message);
        log.info("Activation email sent!!: {} \n", user.getEmail());

    } catch (Exception exception) {
        log.error("could not process data {}", exception.getMessage());
    }
    }

}
