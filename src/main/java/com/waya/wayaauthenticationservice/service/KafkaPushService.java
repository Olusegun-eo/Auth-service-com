package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.ChatPojo;
import org.springframework.http.ResponseEntity;

public interface KafkaPushService {

    ResponseEntity postChat(ChatPojo chatPojo);
}
