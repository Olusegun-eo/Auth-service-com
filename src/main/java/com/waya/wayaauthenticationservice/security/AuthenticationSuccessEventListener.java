package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.service.FraudService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final FraudService fraudService;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent event) {
        Object obj = event.getAuthentication().getPrincipal();
        if(obj!= null && obj instanceof UserPrincipal){
            UserPrincipal principal =  (UserPrincipal) obj;
            log.info("Login Success :: {}", principal);
            Users user = principal.getUser().orElse(null);
            fraudService.actionOnSignInSuccess(user);
        }

    }
}