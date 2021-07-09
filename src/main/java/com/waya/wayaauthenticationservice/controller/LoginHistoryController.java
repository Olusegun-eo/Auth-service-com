package com.waya.wayaauthenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.pojo.LoginHistoryPojo;
import com.waya.wayaauthenticationservice.service.LoginHistoryService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/history")
@Tag(name = "AUTH HISTORY", description = "Auth History API")
public class LoginHistoryController {

    @Autowired
    LoginHistoryService loginHistoryService;

    @ApiOperation(value="Save Login History",tags = { "AUTH HISTORY" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/save")
    public ResponseEntity<?> saveHistory(@RequestBody LoginHistoryPojo loginHistoryPojo) {
        return loginHistoryService.saveHistory(loginHistoryPojo);
    }

    @ApiOperation(value="Get My Login History",tags = { "AUTH HISTORY" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/my-history")
    public ResponseEntity<?> getMyHistory() {
        return loginHistoryService.getHistoryByUser();
    }


    @ApiOperation(value="Get My Last Login History",tags = { "AUTH HISTORY" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/my-last-login")
    public ResponseEntity<?> getMyLastHistory() {
        return loginHistoryService.getMYLastHistory();
    }


    @ApiOperation(value="Get User Login History",tags = { "AUTH HISTORY" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/user-history/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable long userId) {
        return loginHistoryService.getHistoryByUserId(userId);
    }

    @ApiOperation(value="Get User Last Login History",tags = { "AUTH HISTORY" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/user-last-login/{userId}")
    public ResponseEntity<?> getLastUserHistory(@PathVariable long userId) {
        return loginHistoryService.getLastHistoryByUserId(userId);
    }

    @ApiOperation(value="Get All History (Admin Only)" ,tags = { "AUTH HISTORY" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping(path = "/all-history")
    public ResponseEntity<?> getAllUserHistory() {
        return loginHistoryService.getAllHistoryByAdmin();
    }

}
