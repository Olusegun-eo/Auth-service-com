package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserPojo user) {
        return authenticationService.create(user);
    }

    @ApiOperation("User login")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/login")
    public void Login(@RequestBody LoginDetailsPojo loginRequestModel) {

        throw new IllegalStateException("This Method should not be called!");
    }

//    public ResponseEntity<?> assignRoleToUser(@PathVariable("userId") Long userId, @PathVariable("roleList") List<Integer> roleIds) {
//
//    }

}
