package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.SecurityConstants;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/user")
@EnableCaching
public class UserController {

    @Autowired
    RedisUserDao dao;

    @Autowired
    UserService userService;

    @ApiOperation(value = "Save users to redis", hidden = false)
    @PostMapping
    public RedisUser save(@RequestBody RedisUser redisUser) {
        return dao.save(redisUser);
    }

    @ApiOperation(value = "Get all users from redis", hidden = false)
    @GetMapping
    public List<RedisUser> getAllUsers() {
        return dao.findAll();
    }

    @ApiOperation("Get User Details and Roles by ID from Redis (In-app use only)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/{id}")
//    @Cacheable(key = "#id",value = "User")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
    	System.out.println("::::Controller::::");
        return userService.getUserById(id);
    }


    @ApiOperation("Get User Details by Email (In-app use only)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("email/{email}")
    public ResponseEntity getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @ApiOperation("Get User Details by Phone (In-app use only)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @GetMapping("phone/{phone}")
    public ResponseEntity getUserByPhone(@PathVariable String phone, HttpServletRequest req) {
        return userService.getUserByPhone(phone, req.getHeader("Authorization"));
    }

    @ApiOperation("Phone Contact check  (Service consumption only. Do not Use)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/contact-check", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> contactCheck(@RequestBody ContactPojoReq contacts) {
        return userService.wayaContactCheck(contacts);
    }

    @ApiOperation(value = "Get my Info change", notes = "This endpointis used by logged in users to fetch their info")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/myinfo")
    public ResponseEntity getMyInfo() {
        return userService.getMyInfo();
    }

    @ApiOperation(value = "Delete User profiles", notes = "Disable user's accounts")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity remove(@PathVariable Long id,@RequestHeader("Authorization") String authorization) {
        return userService.deleteUser(id,authorization);
    }
    
    @ApiOperation(value = "Get Users By role name count", notes = "This endpointis used to get all users by role name, it returns an integer of count")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/count/{role}")
    public ResponseEntity<Integer> findAllUserByRoleCount(@PathVariable("role") String role) {
    	return ResponseEntity.ok(userService.getUsersCount(role));
    }
    
    
    @ApiOperation(value = "Edit User Details", notes = "This endpointis used update user details")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PutMapping("/update")
    public ResponseEntity<UserRoleUpdateRequest> updateUser(@RequestBody UserRoleUpdateRequest user) {
    	
    	return ResponseEntity.ok(userService.UpdateUser(user));
    }
    
    @ApiOperation(value = "Get user details for role service consumption", notes = "This endpointis used to get user details for Role Service")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/get/user/for/role/{id}")
    public ResponseEntity<UserEditPojo> getUserForRole(@PathVariable("id") Long id) {
    	return ResponseEntity.ok(userService.getUserForRole(id));
    }


    
}
