package com.waya.wayaauthenticationservice.pojo.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponsePojo {
    private String eventType;
    private String initiator;
    private String eventCategory;
    private DataPojo data;

}
