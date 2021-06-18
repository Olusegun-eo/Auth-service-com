package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends UserDetailsService {
    Collection<? extends GrantedAuthority> getAuthorities(Collection<Roles> roles);
    ResponseEntity getUser(Long userId);
    ResponseEntity getUsers();
    ResponseEntity getUsersByRole(int roleId);
    ResponseEntity getUserByEmail(String email);
    ResponseEntity getUserByPhone(String phone, String token);
    ResponseEntity getUserById(Long id);
    ResponseEntity deleteUser(Long id,String token);
    ResponseEntity wayaContactCheck(ContactPojoReq contacts);
    ResponseEntity getMyInfo();
    Integer getUsersCount(String roleName);
    UserRoleUpdateRequest UpdateUser(UserRoleUpdateRequest user);
    //Get user details for Roles service
    UserEditPojo getUserForRole(Long id);
}
