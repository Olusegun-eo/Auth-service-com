package com.waya.wayaauthenticationservice.pojo.notification;

import com.waya.wayaauthenticationservice.enums.EventCategory;
import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotificationResponsePojo {
    private String eventType;
    private String initiator;
    private EventCategory eventCategory;
    private StreamDataEmail data;
    private String productType = "WAYABANK";

}