package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.assembler.UserAssembler;
import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.others.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.others.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.List;

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

    @Autowired
    PagedResourcesAssembler<Users> pagedResourcesAssembler;

    @Autowired
    UserAssembler userAssembler;

    @ApiOperation(value = "Save users to redis", hidden = false, tags = {
            "USER SERVICE"})
    @PostMapping
    public RedisUser save(@RequestBody RedisUser redisUser) {
        return dao.save(redisUser);
    }

    @ApiOperation(value = "Get all users from redis", hidden = false, tags = {"USER SERVICE"})
    @GetMapping
    public List<RedisUser> getAllUsers() {
        return dao.findAll();
    }

    @ApiOperation(value = "Get User Details and Roles by ID (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("/{id}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#id, authentication)")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @ApiOperation(value = "Get User Details by Email (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("email/{email}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
    public ResponseEntity<?> getUserByEmail(@PathVariable @Email String email) {
        return userService.getUserByEmail(email);
    }
    
    @ApiOperation(value = "Get User Details by Email (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("email/{email}/key")
    public ResponseEntity<?> getUserByEmailKey(@PathVariable @Email String email) {
        return userService.getUserByEmail(email);
    }

    @ApiOperation(value = "Get User Details by Phone (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("phone/{phone}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#phone, authentication)")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phone) {
        return userService.getUserByPhone(phone);
    }
    
    @ApiOperation(value = "Get User Details by Phone (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("phone/{phone}/key")
    public ResponseEntity<?> getUserByPhoneKey(@PathVariable String phone) {
        return userService.getUserByPhone(phone);
    }

    @ApiOperation(value = "Get User by Phone (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("walletByPhone")
    public ResponseEntity<?> getUserByPhoneForService(@RequestParam("phone") String phone) {
        return userService.getUserInfoByPhoneOrEmailForServiceConsumption(phone.trim());
    }

    @ApiOperation(value = "Get User by Email (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("walletByEmail")
    public ResponseEntity<?> getUserByEmailForService(@RequestParam("email") @Email String email) {
        return userService.getUserInfoByPhoneOrEmailForServiceConsumption(email.trim());
    }

    @ApiOperation(value = "Get User Details by UserId (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("walletByUserId")
    public ResponseEntity<?> getUserByIdForService(@RequestParam("id") Long userId) {
        return userService.getUserInfoByUserIdForServiceConsumption(userId);
    }

    @ApiOperation(value = "Phone Contact check  (Service consumption only. Do not Use)", tags = {"USER SERVICE"})
    @PostMapping(path = "/contact-check", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> contactCheck(@RequestBody ContactPojoReq contacts) {
        return userService.wayaContactCheck(contacts);
    }

    @ApiOperation(value = "Get my Info change", notes = "This endpoint is used by logged in users to fetch their info", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo() {
        return userService.getMyInfo();
    }

    @ApiOperation(value = "Delete User profiles", notes = "Disable user's accounts", tags = {"USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @DeleteMapping("/delete/{id}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#id, authentication)")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @ApiOperation(value = "To Reverse Delete User profiles", notes = "Enables user's accounts", tags = {"USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @PostMapping("/reverse-delete/{id}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#id, authentication)")
    public ResponseEntity<?> reverseDelete(@PathVariable Long id) {
        return userService.unDeleteUser(id);
    }

    @ApiOperation(value = "Edit User Role Details (Service consumption only. Do not Use)", notes = "This endpoint is used update user details", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @PutMapping("/role/update")
    public ResponseEntity<?> updateUser(@RequestBody UserRoleUpdateRequest user) {
        return ResponseEntity.ok(userService.UpdateUser(user));
    }

    @ApiOperation(value = "Get Users By role name count", notes = "This endpoint is used to get all users by role name, it returns an integer of count", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class,value = "token", paramType = "header", required = true)})
    @GetMapping("/count/{role}")
    public ResponseEntity<Integer> findAllUserByRoleCount(@PathVariable("role") String role) {
        return ResponseEntity.ok(userService.getUsersCount(role));
    }

    @ApiOperation(value = "Get user details for role service consumption", notes = "This endpoint is used to get user details for Role Service", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @GetMapping("/get/user/for/role/{id}")
    public ResponseEntity<UserEditPojo> getUserForRole(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserForRole(id));
    }

    @ApiOperation(value = "To toggle Activation of Users Account by UserId (For In App service consumption)",
            notes = "To toggle Activation of Users Account by UserId (For In App service consumption)", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @PostMapping("toggle-activation/{userId}")
    public  ResponseEntity<?> toggleActivateUserAccount(@PathVariable("userId") Long userId){
        return userService.toggleActivation(userId);
    }

    @ApiOperation(value = "To toggle Account Lock of Users Account by UserId (For In App service consumption)",
            notes = "To toggle Account Lock of Users Account by UserId (For In App service consumption)", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @PostMapping("toggle-lock/{userId}")
    public  ResponseEntity<?> toggleLockUserAccount(@PathVariable("userId") Long userId){
        return userService.toggleLock(userId);
    }
    
    @ApiOperation(value = "Get Users Setup (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("/setup")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#id, authentication)")
    public ResponseEntity<?> fetchUserSetUp(@RequestParam("id") Long id) {
        return userService.getUserSetupById(id);
    }
    
    @ApiOperation(value = "Create/Update Users Setup (In-app use only)", tags = {"USER SERVICE"})
    @PostMapping("/setup")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#pojo.userId, authentication)")
    public ResponseEntity<?> maintainUserSetUp(@Valid @RequestBody UserSetupPojo pojo) {
        return userService.maintainUserSetup(pojo);
    }
    
    @ApiOperation(value = "Get Users Statistics (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("/statistics")
    //@PreAuthorize(value = "@userSecurity.useHierarchy(#pojo.userId, authentication)")
    public ResponseEntity<?> UserStatistics(@RequestParam("page") final int page, 
			@RequestParam("size") final int size, @RequestParam("sortBy") final String sortBy, 
			@RequestParam("sortOrder") final String sortOrder) {
        return userService.GetUserStatistics(page, size, sortBy, sortOrder);
    }
    
    @ApiOperation(value = "Get Users Statistics for Individual Corporate (In-app use only)", tags = {"USER SERVICE"})
    @GetMapping("/statistics/indvCorp/{userType}")
    //@PreAuthorize(value = "@userSecurity.useHierarchy(#pojo.userId, authentication)")
    public ResponseEntity<?> UserStatisticsforIndvCorp(@RequestParam("page") final int page, 
			@RequestParam("size") final int size, @PathVariable("userType") String userType) {
        return userService.GetUserStatisticsforIndvCorp(page, size, userType);
    }


    @ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "USER SERVICE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
    @GetMapping("/users")
    public ResponseEntity<PagedModel<UserProfileResponsePojo>> getAllUsersDB(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Users> userPagedList = userService.getAllUsers(page, size);
        PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(userPagedList,
                userAssembler);
        return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
    }

}
