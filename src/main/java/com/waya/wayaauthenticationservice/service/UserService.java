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
    ResponseEntity getUsersByRole(int roleId);
    ResponseEntity getUserByEmail(String email);
    ResponseEntity getUserByPhone(String phone);
    ResponseEntity getUserById(Long id);
    ResponseEntity deleteUser(Long id);
    ResponseEntity wayaContactCheck(ContactPojoReq contacts);
    ResponseEntity getMyInfo();
    Integer getUsersCount(String roleName);
}
