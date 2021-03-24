package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {
    ResponseEntity getCorporateUsers();
    ResponseEntity getUsersByRole(long roleId);
    ResponseEntity createUser(UserPojo userPojo, long roleId);
}
