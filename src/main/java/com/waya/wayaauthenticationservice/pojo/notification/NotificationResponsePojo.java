package com.waya.wayaauthenticationservice.pojo.notification;

import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class NotificationResponsePojo {
    private String eventType;
    private String initiator;
    private String eventCategory;
    private StreamDataEmail data;
    private String productType;
}
