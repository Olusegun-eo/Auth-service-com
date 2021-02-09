package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;

public interface UserService extends UserDetailsService {
    Collection<? extends GrantedAuthority> getAuthorities(Collection<Roles> roles);
    ResponseEntity getUser(Long userId);
    ResponseEntity getUsers();
    ResponseEntity getUserByEmail(String email);
    ResponseEntity getUserByPhone(String phone);
    ResponseEntity wayaContactCheck(ContactPojoReq contacts);
    ResponseEntity getMyInfo();
}
