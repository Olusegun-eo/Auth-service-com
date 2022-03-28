package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.FraudService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UserRepository userRepository;
    private final FraudService fraudService;

    @Override
    public void onApplicationEvent(final AuthenticationFailureBadCredentialsEvent event) {
        log.error("Login Failed: {} - {}", event.getException().getMessage(), event.getAuthentication());
        Object obj = event.getAuthentication().getPrincipal();
        String principal = Objects.toString(obj);
        System.out.println("principal :: {} " + principal);
        Users user = userRepository.findByEmailOrPhoneNumber(principal).orElse(null);
        System.out.println("Users :: {} " + user);
        fraudService.actionOnInvalidPassword(user);
    }
}