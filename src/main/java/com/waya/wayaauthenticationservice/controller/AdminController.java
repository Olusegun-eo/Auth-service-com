package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.assembler.UserAssembler;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.*;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "ADMIN", description = "Admin Authentication Service API")
@EnableCaching
@PreAuthorize(value = "hasRole('APP_ADMIN')")
@Validated
public class AdminController {

    @Autowired
    RedisUserDao dao;

    @Autowired
    UserService userService;

    @Autowired
    AdminService adminService;

    @Autowired
    PagedResourcesAssembler<Users> pagedResourcesAssembler;

    @Autowired
    UserAssembler userAssembler;

    @Autowired
    ProfileService profileService;

    @ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users")
    public ResponseEntity<PagedModel<UserProfileResponsePojo>> getAllUsersDB(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Users> userPagedList = userService.getAllUsers(page, size);
        PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(userPagedList, userAssembler);
        return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
    }

    @ApiOperation(value = "Create New Private User (Admin Endpoint)", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/users/create-private")
    public ResponseEntity<?> createNewPrivateUser(@Valid @RequestBody BaseUserPojo userPojo, HttpServletRequest request, Device device) {
        return adminService.createUser(userPojo, request, device);
    }

    @ApiOperation(value = "Create New Corporate User (Admin Endpoint)", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/users/create-corporate")
    public ResponseEntity<?> createNewCorporateUser(@Valid @RequestBody CorporateUserPojo userPojo, HttpServletRequest request, Device device) {
        return adminService.createUser(userPojo, request, device);
    }

    @ApiOperation(value = "Bulk Private User Registration", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/users/bulk-user/private", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createBulkPrivateUsers(@Valid @RequestBody BulkPrivateUserCreationDTO userList, HttpServletRequest request, Device device) {
        return userService.createUsers(userList, request, device);
    }

    @ApiOperation(value = "Bulk Corporate User Registration", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/users/bulk-user/corporate", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createBulkCorporateUsers(@Valid @RequestBody BulkCorporateUserCreationDTO userList, HttpServletRequest request, Device device) {
        return userService.createUsers(userList, request, device);
    }

    @ApiOperation(value = "Bulk User Registration", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/users/bulk-user-excel/{isCorporate}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBulkUserExcel(@RequestPart("file") MultipartFile file,
                                                 @PathVariable(value = "isCorporate") boolean isCorporate,
                                                 HttpServletRequest request, Device device) {
        return adminService.createBulkUser(file, isCorporate, request, device);
    }

    @ApiOperation(value = "Download Template for Bulk User Creation ", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/download/bulk-user-excel")
    public ResponseEntity<Resource> getFile(@RequestParam("isCorporate") boolean isCorporate) {
        String filename = "bulk-user-excel.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.createExcelSheet(isCorporate));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                //.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @ApiOperation(value = "Fetch Users by Roles (Admin Endpoint)", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users/role/{roleId}")
    public ResponseEntity<?> getUsersByRole(@PathVariable int roleId,
                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Users> usersPage = adminService.getUsersByRole(roleId, page, size);
        PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(usersPage, userAssembler);
        return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
    }

    @ApiOperation(value = "Fetch Users By Corporate/Non-Corporate (Admin Endpoint)", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/users/corporate/{isCorporate}")
    public ResponseEntity<?> findAllCorporateUsers(@PathVariable("isCorporate") boolean isCorporate,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Users> usersPage = adminService.getCorporateUsers(isCorporate, page, size);
        PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(usersPage, userAssembler);
        return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
    }

    @ApiOperation(value = "Admin Should be able to update Corporate Profile on behalf of the user", tags = {"ADMIN"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("/update-corporate-profile/{userId}")
    ResponseEntity<?> updateCorporateProfile(
            @Valid @RequestBody UpdateCorporateProfileRequest updateCorporateProfileRequest,
            @PathVariable String userId){
        UserProfileResponse corporateProfileResponse = profileService.updateProfile(updateCorporateProfileRequest, userId);
        com.waya.wayaauthenticationservice.response.ApiResponse<UserProfileResponse> response = new com.waya.wayaauthenticationservice.response.ApiResponse<>(corporateProfileResponse,
                "profile updated successfully", true);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @ApiOperation(value = "Admin Should be able to update Personal Profile on behalf of the user", tags = {"ADMIN"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("/update-personal-profile/{userId}")
    ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdatePersonalProfileRequest updatePersonalProfileRequest,
            @PathVariable String userId){
        UserProfileResponse profileResponse = profileService.updateProfile(updatePersonalProfileRequest, userId);
        return new ResponseEntity<>(new com.waya.wayaauthenticationservice.response.ApiResponse<>(profileResponse,
                "profile updated successfully", true), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Send OTP to email and phoneNumber for Waya Account Viewing",
            notes = "Send OTP to email and phoneNumber for Waya Account Viewing", tags = {"ADMIN"})
    @GetMapping("/authenticate-waya/otp-send")
    @PreAuthorize(value = "hasAuthority('ROLE_APP_ADMIN')")
    public ResponseEntity<?> sendAdminOTP() {
        return adminService.sendAdminOTP();
    }

    @ApiOperation(value = "Send OTP to email and phoneNumber for Waya Account Viewing",
            notes = "Send OTP to email and phoneNumber for Waya Account Viewing", tags = {"ADMIN"})
    @PostMapping("/authenticate-waya/otp-verify/{otp}")
    @PreAuthorize(value = "hasAuthority('ROLE_APP_ADMIN')")
    public ResponseEntity<?> verifyAdminOTP(@PathVariable Integer otp) {
        return adminService.verifyAdminOTP(otp);
    }

    public  ResponseEntity<?> deactivateUserAccount(){

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    public  ResponseEntity<?> activateUserAccount(){

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
    public  ResponseEntity<?> signOutUser(){

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    //ability to manage account details and profile on behalf of the user�delete, edit, sign out , deactivate, activate

}
