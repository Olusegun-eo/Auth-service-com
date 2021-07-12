package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.pojo.ChatPojo;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.service.KafkaPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.waya.wayaauthenticationservice.util.Constant.CHAT_TOPIC;


@Service
public class KafkaPushServiceImpl implements KafkaPushService {


    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @Override
    public ResponseEntity<SuccessResponse> postChat(ChatPojo chatPojo) {
        kafkaMessageProducer.send(CHAT_TOPIC, chatPojo);
        return new ResponseEntity<>(new SuccessResponse("Pushed to Kafka", null), HttpStatus.OK);
    }
}
