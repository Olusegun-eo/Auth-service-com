package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.PinPojo;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.response.ResponseHelper;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity createUser(UserPojo userPojo);
    ResponseEntity createPin(PinPojo pinPojo);
    ResponseEntity verifyOTP(UserPojo userPojo);

}
