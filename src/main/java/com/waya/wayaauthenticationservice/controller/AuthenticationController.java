package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.ChangePasswordPojo;
import com.waya.wayaauthenticationservice.pojo.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.PinPojo;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.service.impl.AuthenticationServiceImpl;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationServiceImpl authenticationServiceImpl;


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserPojo user) {
        return authenticationServiceImpl.createUser(user);
    }


    @ApiOperation("User login")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/login")
    public void Login(@RequestBody LoginDetailsPojo loginRequestModel) {
        throw new IllegalStateException("This Method should not be called!");
    }

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @PostMapping("/create/pin")
    public ResponseEntity<?> createPin(@RequestBody PinPojo pinPojo) {
        return authenticationServiceImpl.createPin(pinPojo);
    }


}
