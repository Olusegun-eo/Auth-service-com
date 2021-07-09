package com.waya.wayaauthenticationservice.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.AuthenticationService;


@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Override
    public ResponseEntity<?> getCorporateUsers() {
        return null;
    }

    @Override
    public ResponseEntity<?> getUsersByRole(long roleId) {
        return null;
    }

    @Override
    public ResponseEntity<?> createUser(UserPojo userPojo, long roleId,HttpServletRequest request,Device device) {
        ResponseEntity<?> response = authenticationService.createUser(userPojo,request,device);
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST){
            return response;
        }
        return  response;
    }


}
