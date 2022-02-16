package com.waya.wayaauthenticationservice.proxy;


import com.waya.wayaauthenticationservice.pojo.notification.NotificationResponsePojo;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE-API")
public interface NotificationProxy {

    @Headers("Content-Type: application/json")
    @PostMapping("/email-notification")
    NotificationResponsePojo sendEmail(@RequestBody NotificationResponsePojo notificationResponsePojo);

}
