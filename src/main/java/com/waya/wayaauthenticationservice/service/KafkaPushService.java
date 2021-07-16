package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.others.ChatPojo;
import org.springframework.http.ResponseEntity;

public interface KafkaPushService {

    ResponseEntity<?> postChat(ChatPojo chatPojo);
}
