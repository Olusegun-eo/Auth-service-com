package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.others.SuperAdminCreatUserRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import javax.servlet.http.HttpServletRequest;

public interface SuperAdminService {

    ResponseEntity<?> createUser(SuperAdminCreatUserRequest userPojo, HttpServletRequest request, Device device);
    ResponseEntity<String> testEmailTemplate();
    ResponseEntity<String> testOTPEmailTemplate();
    ResponseEntity<String> testPinReset();
}
