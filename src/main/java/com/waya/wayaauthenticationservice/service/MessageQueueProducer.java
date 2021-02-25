package com.waya.wayaauthenticationservice.service;

public interface MessageQueueProducer {

    void send(String topic, Object data);

}
