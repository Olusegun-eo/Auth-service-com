package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.security.AuthenticationFilter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER= LoggerFactory.getLogger(AuthenticationFilter.class);


    public ResponseEntity create(UserPojo mUser) {
        try {
            Roles roles = new Roles();
            roles.setId(1);
            roles.setName("User");
            Roles mRoles = rolesRepo.save(roles);
            List<Roles> roleList = new ArrayList<>();
            roleList.add(mRoles);
            Users user = new ModelMapper().map(mUser, Users.class);
            user.setId(0L);
            user.setDateCreated(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(mUser.getPassword()));
            user.setRolesList(roleList);
            userRepo.save(user);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

//    public ResponseEntity<?> assignRoleToUser(Long userId, List<Integer> rolesId) {
//        try {
//            return userRepo.findById(userId).map(users -> {
//                rolesRepo.findAll().stream().filter(r -> rolesId.contains(r.getId()))
//                        .filter(rl -> !users.getRolesList().contains(rl.getId()))
//                        .forEach(
//                               System.out::println
//                        );
////                rolesRepo.findById(rolesId).ifPresent(roles -> {
////                    users.getRolesList().add(roles);
////                    userRepo.save(users);
////                });
////                rolesId.stream().map(integer -> {
////                    if (rolesRepo.findById(integer).isPresent()){
////                        users.getRolesList().forEach(rl -> {
////                            if (!rl.getId().equals(integer)) {
////                                users.getRolesList().add(rl);
////                            }
////                        });
////                    }
////                    return null;
////                });
//                return new ResponseEntity<>(HttpStatus.OK);
//            }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//        } catch (Exception e) {
//            LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
//            return new ResponseEntity(HttpStatus.BAD_REQUEST);
//        }
//    }

}
