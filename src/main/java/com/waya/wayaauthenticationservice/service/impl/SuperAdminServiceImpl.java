package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.Users;
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

}
