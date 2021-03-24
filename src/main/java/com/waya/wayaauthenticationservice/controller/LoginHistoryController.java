package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.LoginHistoryPojo;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.LoginHistoryService;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class LoginHistoryController {

    @Autowired
    LoginHistoryService loginHistoryService;

    @ApiOperation("Save Login History")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/save")
    public ResponseEntity<?> saveHistory(@RequestBody LoginHistoryPojo loginHistoryPojo) {
        return loginHistoryService.saveHistory(loginHistoryPojo);
    }

    @ApiOperation("Get My Login History")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/my-history")
    public ResponseEntity<?> getMyHistory() {
        return loginHistoryService.getHistoryByUser();
    }


    @ApiOperation("Get My Last Login History")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/my-last-login")
    public ResponseEntity<?> getMyLastHistory() {
        return loginHistoryService.getMYLastHistory();
    }


    @ApiOperation("Get User Login History")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/user-history/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable long userId) {
        return loginHistoryService.getHistoryByUserId(userId);
    }

    @ApiOperation("Get User Last Login History")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/user-last-login/{userId}")
    public ResponseEntity<?> getLastUserHistory(@PathVariable long userId) {
        return loginHistoryService.getLastHistoryByUserId(userId);
    }

    @ApiOperation("Get All History (Admin Only)")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/all-history")
    public ResponseEntity<?> getAllUserHistory() {
        return loginHistoryService.getAllHistoryByAdmin();
    }

}
