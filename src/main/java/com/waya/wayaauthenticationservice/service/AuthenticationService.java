package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RolesRepository rolesRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public ResponseEntity create(UserPojo mUser) {
        try {
            Roles roles = new Roles();
            roles.setId(1);
            roles.setName("User");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);
            Users user = new ModelMapper().map(mUser, Users.class);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            userRepo.save(user);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

}
