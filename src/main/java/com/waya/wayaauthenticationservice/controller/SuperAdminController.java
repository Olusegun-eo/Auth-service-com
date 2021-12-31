package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.others.SuperAdminCreatUserRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/super/user")
@Tag(name = "SUPER ADMIN USER SERVICE", description = "Super Admin User Service API")
@EnableCaching
public class SuperAdminController {

    @Autowired
    SuperAdminService superAdminService;

    @ApiOperation(value = "Create New Admin User Account (Super Admin Endpoint). Only a user with Owner Role can execute", tags = {
            "SUPER-ADMIN" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
    @PostMapping("/users/waya-account")
    @PreAuthorize(value = "hasAuthority('ROLE_OWNER_ADMIN')")
    public ResponseEntity<?> createAdminAccount(@Valid @RequestBody SuperAdminCreatUserRequest userPojo,
                                                       HttpServletRequest request, Device device) {
        userPojo.setWayaAdmin(true);
        return superAdminService.createUser(userPojo, request, device);
    }




}
