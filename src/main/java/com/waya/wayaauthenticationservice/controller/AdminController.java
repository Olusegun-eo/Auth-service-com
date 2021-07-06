package com.waya.wayaauthenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.assembler.UserAssembler;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "ADMIN", description = "Admin Authentication Service API")
@EnableCaching
@PreAuthorize(value = "hasRole('ADMIN')")
public class AdminController {

    @Autowired
    RedisUserDao dao;

    @Autowired
    UserService userService;

    @Autowired
    PagedResourcesAssembler<Users> pagedResourcesAssembler;

    @Autowired
    UserAssembler userAssembler;

    @ApiOperation(value = "Fetch all Users (Admin Endpoint)" ,tags = { "ADMIN" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users")
    public ResponseEntity<PagedModel<UserProfileResponsePojo>> getAllUsersDB(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Users> userPagedList = userService.getAllUsers(page, size);
        PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(userPagedList, userAssembler);
        return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
    }


    @ApiOperation(value = "Fetch Users by Roles (Admin Endpoint)",tags = { "ADMIN" })
    //@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users/byrole/{roleId}")
    public ResponseEntity<?> getUsersByRole(@PathVariable int roleId) {
        return userService.getUsersByRole(roleId);
    }


}
