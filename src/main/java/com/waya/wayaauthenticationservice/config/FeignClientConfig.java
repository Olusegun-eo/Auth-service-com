package com.waya.wayaauthenticationservice.config;

import com.waya.wayaauthenticationservice.service.FileResourceServiceFeignClient;
import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@EnableFeignClients(clients = {FileResourceServiceFeignClient.class})
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }

}
