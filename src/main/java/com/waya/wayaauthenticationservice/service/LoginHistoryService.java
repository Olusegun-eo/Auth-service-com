package com.waya.wayaauthenticationservice.service;

import org.springframework.http.ResponseEntity;

import com.waya.wayaauthenticationservice.pojo.others.LoginHistoryPojo;

public interface LoginHistoryService {

    ResponseEntity<?> saveHistory(LoginHistoryPojo loginHistoryPojo);
    ResponseEntity<?> getHistoryByUserId(long userId);
    ResponseEntity<?> getHistoryByUser();
    ResponseEntity<?> getLastHistoryByUserId(long userId);
    ResponseEntity<?> getMYLastHistory();
    ResponseEntity<?> getAllHistoryByAdmin();
}
