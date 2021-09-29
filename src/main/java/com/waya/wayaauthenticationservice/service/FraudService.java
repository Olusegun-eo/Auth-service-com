package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.fraud.FraudIDPojo;
import org.springframework.http.ResponseEntity;

public interface FraudService {

    void actionOnInvalidPassword(Users user);

    void actionOnSignInSuccess(Users user);

    void actionOnPinValidateSuccess(Users user);

    void actionOnInvalidPin(Users user);

    ResponseEntity<?> findUserById(Long id, String apiKey);

    ResponseEntity<?> toggleUserLock(FraudIDPojo pojo);

    ResponseEntity<?> toggleUserActivation(FraudIDPojo pojo);

    ResponseEntity<?> closeAccount(FraudIDPojo pojo);
}
