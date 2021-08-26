package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
        if (this.getAuthentication() != null && this.getAuthentication().getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) this.getAuthentication().getPrincipal();
            return userPrincipal.getUser().orElse(null);
        }
        return null;
    }
}