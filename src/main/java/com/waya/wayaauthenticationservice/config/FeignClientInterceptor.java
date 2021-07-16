package com.waya.wayaauthenticationservice.config;

import com.waya.wayaauthenticationservice.security.UserPrincipal;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthenticationService authenticationService;

    public FeignClientInterceptor(@Lazy AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
            if (details.getUser().isPresent()) {
                String token = this.authenticationService.generateToken(details.getUser().get());
                template.header(AUTHORIZATION_HEADER, token);
            }
        }
    }
}
