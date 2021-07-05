package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserFacadeImpl implements AuthenticatedUserFacade {

    @Autowired
    private UserRepository userRepo;

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