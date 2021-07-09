package com.waya.wayaauthenticationservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.entity.Users;

@Component("authenticatedUserFacade")
public class AuthenticatedUserFacadeImpl implements AuthenticatedUserFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public String getName() {
        return this.getAuthentication().getName();
    }

    @Override
    public Users getUser() {
        UserPrincipal userPrincipal = (UserPrincipal) this.getAuthentication().getPrincipal();
        if(userPrincipal != null){
            return userPrincipal.getUser().orElse(null);
        }
        return null;
    }
}