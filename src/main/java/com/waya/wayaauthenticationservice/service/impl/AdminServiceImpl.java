package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Override
    public ResponseEntity getCorporateUsers() {
        return null;
    }

    @Override
    public ResponseEntity getUsersByRole(long roleId) {
        return null;
    }

    @Override
    public ResponseEntity createUser(UserPojo userPojo, long roleId) {
        ResponseEntity response = authenticationService.createUser(userPojo);
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST){
            return response;
        }
        return  response;
    }


}
