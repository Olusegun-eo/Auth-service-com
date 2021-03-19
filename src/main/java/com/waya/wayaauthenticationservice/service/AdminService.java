package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {

    ResponseEntity getCorporateUsers();
    ResponseEntity getUsersByRole(int roleId);
}
