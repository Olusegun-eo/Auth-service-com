package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
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
    @Cacheable(key = "#id",value = "User")
    public RedisUser findUser(@PathVariable int id) {
        return dao.findUserById(id);
    }


    @ApiOperation("Get User Details by Email (In-app use only)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("email/{email}")
    public ResponseEntity getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }


    @ApiOperation(value = "Get my Info", notes = "This endpointis used by logged in users to fetch their info")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/myinfo")
    public ResponseEntity getMyInfo() {
        return userService.getMyInfo();
    }

    @ApiOperation(value = "Delete from redis", hidden = true)
    @DeleteMapping("/{id}")
    @CacheEvict(key = "#id",value = "User")
    public String remove(@PathVariable int id) {
        return dao.deleteUser(id);
    }
}
