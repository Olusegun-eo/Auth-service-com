package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.pojo.others.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.others.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.others.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "Get User Details and Roles by ID from Redis (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/{id}")
    @Cacheable(key = "#id", value = "User")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#id, authentication)")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @ApiOperation(value = "Get User Details by Email (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("email/{email}")
    @Cacheable(key = "#email", value = "User")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @ApiOperation(value = "Get User Details by Phone (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("phone/{phone}")
    @Cacheable(key = "#phone", value = "User")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#phone, authentication)")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phone) {
        return userService.getUserByPhone(phone);
    }

    @ApiOperation(value = "Get User and Wallet Details by Phone (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("walletByPhone")
    @Cacheable(key = "#phone", value = "UserWallet")
    public ResponseEntity<?> getUserAndWalletByPhone(@RequestParam("phone") String phone) {
        return userService.getUserAndWalletByPhoneOrEmail(phone.trim());
    }

    @ApiOperation(value = "Get User and Wallet Details by Email (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("walletByEmail")
    @Cacheable(key = "#email", value = "UserWallet")
    public ResponseEntity<?> getUserAndWalletByEmail(@RequestParam("email") String email) {
        return userService.getUserAndWalletByPhoneOrEmail(email.trim());
    }

    @ApiOperation(value = "Get User and Wallet Details by UserId (In-app use only)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("walletByUserId")
    @Cacheable(key = "#id", value = "UserWallet")
    public ResponseEntity<?> getUserAndWalletById(@RequestParam("id") Long userId) {
        return userService.getUserAndWalletByUserId(userId);
    }

    @ApiOperation(value = "Phone Contact check  (Service consumption only. Do not Use)", tags = {"USER SERVICE"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/contact-check", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> contactCheck(@RequestBody ContactPojoReq contacts) {
        return userService.wayaContactCheck(contacts);
    }

    @ApiOperation(value = "Get my Info change", notes = "This endpointis used by logged in users to fetch their info", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo() {
        return userService.getMyInfo();
    }

    @ApiOperation(value = "Delete User profiles", notes = "Disable user's accounts", tags = {"USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @ApiOperation(value = "To Reverse Delete User profiles", notes = "Enables user's accounts", tags = {"USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/reverse-delete/{id}")
    public ResponseEntity<?> reverseDelete(@PathVariable Long id) {
        return userService.unDeleteUser(id);
    }

    @ApiOperation(value = "Edit User Details", notes = "This endpoint is used update user details", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PutMapping("/role/update")
    public ResponseEntity<?> updateUser(@RequestBody UserRoleUpdateRequest user) {
        return ResponseEntity.ok(userService.UpdateUser(user));
    }

    @ApiOperation(value = "Get Users By role name count", notes = "This endpoint is used to get all users by role name, it returns an integer of count", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/count/{role}")
    public ResponseEntity<Integer> findAllUserByRoleCount(@PathVariable("role") String role) {
        return ResponseEntity.ok(userService.getUsersCount(role));
    }

    @ApiOperation(value = "Get user details for role service consumption", notes = "This endpoint is used to get user details for Role Service", tags = {
            "USER SERVICE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/get/user/for/role/{id}")
    public ResponseEntity<UserEditPojo> getUserForRole(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserForRole(id));
    }

//    @ApiOperation(value = "Admin Update User Details", notes = "This endpoint is used to Update user details", tags = {
//            "USER SERVICE"})
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
//    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
//    @PutMapping("/update-user-details")
//    public ResponseEntity<UserEditPojo> UpdateUserDetails(@Valid @RequestBody UserEditPojo userEditPojo) {
//        return ResponseEntity.ok(userService.UpdateUserDetails(userEditPojo));
//    }

}
