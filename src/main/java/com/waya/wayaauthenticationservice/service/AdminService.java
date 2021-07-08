package com.waya.wayaauthenticationservice.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.pojo.UserPojo;

public interface AdminService {
    ResponseEntity<?> getCorporateUsers();
    ResponseEntity<?> getUsersByRole(long roleId);
    ResponseEntity<?> createUser(UserPojo userPojo, long roleId,HttpServletRequest request,Device device);
}
