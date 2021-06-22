package com.waya.wayaauthenticationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("deleteprofile")
@Setter
@Getter
public class ApplicationConfig {
private String deleteProfileUrl;
private String validateUser;
private String disableWayagramUrl;
}
