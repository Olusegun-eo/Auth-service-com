package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.ChatPojo;
import com.waya.wayaauthenticationservice.pojo.LoginHistoryPojo;
import org.springframework.http.ResponseEntity;

public interface LoginHistoryService {

    ResponseEntity saveHistory(LoginHistoryPojo loginHistoryPojo);
    ResponseEntity getHistoryByUserId(long userId);
    ResponseEntity getHistoryByUser();
    ResponseEntity getLastHistoryByUserId(long userId);
    ResponseEntity getMYLastHistory();
    ResponseEntity getAllHistoryByAdmin();
}
