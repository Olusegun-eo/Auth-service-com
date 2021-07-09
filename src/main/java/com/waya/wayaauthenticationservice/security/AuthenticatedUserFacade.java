package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.security.core.Authentication;

public interface AuthenticatedUserFacade {

    Authentication getAuthentication();

    String getName();

    Users getUser();

}
