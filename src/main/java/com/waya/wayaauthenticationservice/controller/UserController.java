package com.waya.wayaauthenticationservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "USER SERVICE", description = "User Service API")
@EnableCaching
public class UserController {

    @Autowired
    RedisUserDao dao;

    @Autowired
    UserService userService;

    @ApiOperation(value = "Save users to redis", hidden = false ,tags = { "USER SERVICE" })
    @PostMapping
    public RedisUser save(@RequestBody RedisUser redisUser) {
        return dao.save(redisUser);
    }

    @ApiOperation(value = "Get all users from redis", hidden = false ,tags = { "USER SERVICE" })
    @GetMapping
    public List<RedisUser> getAllUsers() {
        return dao.findAll();
    }

    @ApiOperation(value="Get User Details and Roles by ID from Redis (In-app use only)" ,tags = { "USER SERVICE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/{id}")
//    @Cacheable(key = "#id",value = "User")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
    	System.out.println("::::Controller::::");
        return userService.getUserById(id);
    }


    @ApiOperation(value="Get User Details by Email (In-app use only)" ,tags = { "USER SERVICE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("email/{email}")
    public ResponseEntity getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @ApiOperation(value="Get User Details by Phone (In-app use only)" ,tags = { "USER SERVICE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("phone/{phone}")
    public ResponseEntity getUserByPhone(@PathVariable String phone) {
        return userService.getUserByPhone(phone);
    }

    @ApiOperation(value="Phone Contact check  (Service consumption only. Do not Use)" ,tags = { "USER SERVICE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/contact-check", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> contactCheck(@RequestBody ContactPojoReq contacts) {
        return userService.wayaContactCheck(contacts);
    }

    @ApiOperation(value = "Get my Info change", notes = "This endpointis used by logged in users to fetch their info" ,tags = { "USER SERVICE" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/myinfo")
    public ResponseEntity getMyInfo() {
        return userService.getMyInfo();
    }

    @ApiOperation(value = "Delete from redis", notes = "Delete user data" ,tags = { "USER SERVICE" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @DeleteMapping("/delete/{id}")
//    @CacheEvict(key = "#id",value = "User")
    public String remove(@PathVariable int id) {
        return dao.deleteUser(id);
    }
}
