package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository usersRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticatedUserFacade authenticatedUserFacade;


    @Autowired
    private RolesRepository rolesRepo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(Collection<Roles> roles) {
        List<GrantedAuthority> authorities = roles.stream().map(p -> new SimpleGrantedAuthority(p.getName()))
                .collect(toList());
        return authorities;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Users> user = usersRepo.findByEmail(email);

        List<Users> allUsers = usersRepo.findAll();

        if (!user.isPresent()) {
            throw new UsernameNotFoundException(email);
        }else {
            return new org.springframework.security.core.userdetails.User(user.get().getEmail(),
                    user.get().getPassword(), true, true, true, true, new ArrayList<>());
        }
    }

    @Override
    public ResponseEntity getUser(Long userId) {
        Users user = usersRepo.findById(userId).orElse(null);
        if(user == null){
            return new ResponseEntity<>(new ErrorResponse("Invalid Id"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new SuccessResponse("User info fetched", user), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity getUsers() {
        List<Users> users = usersRepo.findAll();
        return new ResponseEntity<>(new SuccessResponse("User info fetched", users), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getUserByEmail(String email) {
        Users user = usersRepo.findByEmail(email).orElse(null);
        if(user == null){
            return new ResponseEntity<>(new ErrorResponse("Invalid email"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new SuccessResponse("User info fetched", user), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity getUserByPhone(String phone) {
        Users user = usersRepo.findByPhoneNumber(phone).orElse(null);
        if(user == null){
            return new ResponseEntity<>(new ErrorResponse("Invalid email"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new SuccessResponse("User info fetched", user), HttpStatus.OK);
        }    }

    @Override
    public ResponseEntity wayaContactCheck(ContactPojoReq contacts) {
        List<ContactPojo> contactPojos = new ArrayList<>();
        for (ContactPojo c: contacts.getContacts()) {
            if (usersRepo.findByPhoneNumber(c.getPhone()).orElse(null) != null){
                c.setWayaUser(true);
            }
            contactPojos.add(c);
        }
        return new ResponseEntity<>(new SuccessResponse("Contact processed", contactPojos), HttpStatus.OK);
    }


    @Override
    public ResponseEntity getMyInfo() {
        Users user = authenticatedUserFacade.getUser();
        return new ResponseEntity<>(new SuccessResponse("User info fetched", user), HttpStatus.OK);
    }

}
