package com.waya.wayaauthenticationservice.response.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponsePojo {
    private String eventType;
    private String initiator;
    private DataPojo data;

}
