package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("admin")
@EnableCaching
public class AdminController {

    @Autowired
    RedisUserDao dao;

    @Autowired
    UserService userService;

    @ApiOperation(value = "Fetch all Users (Admin Endpoint)")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users")
    public ResponseEntity getUsers() {
        return userService.getUsers();
    }


    @ApiOperation(value = "Fetch Users by Roles (Admin Endpoint)")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users/byrole/{roleId}")
    public ResponseEntity getUsersByRole(@PathVariable int roleId) {
        return userService.getUsersByRole(roleId);
    }


}
