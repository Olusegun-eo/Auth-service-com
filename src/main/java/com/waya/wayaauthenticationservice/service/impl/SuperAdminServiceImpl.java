package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.pojo.others.SuperAdminCreatUserRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.SuperAdminService;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;


    @Override
    public ResponseEntity<?> createUser(SuperAdminCreatUserRequest userPojo, HttpServletRequest request, Device device) {
        return authenticationService.superAdminCreateUser(userPojo, request, device, true);
    }


}
