package com.waya.wayaauthenticationservice.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.assembler.UserAssembler;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;
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

	@Autowired
	PagedResourcesAssembler<Users> pagedResourcesAssembler;

	@Autowired
	UserAssembler userAssembler;

	/*
	 * @ApiOperation(value = "Save users to redis", hidden = false, tags = {
	 * "USER SERVICE" })
	 * 
	 * @PostMapping public RedisUser save(@RequestBody RedisUser redisUser) { return
	 * dao.save(redisUser); }
	 * 
	 * @ApiOperation(value = "Get all users from redis", hidden = false, tags = {
	 * "USER SERVICE" })
	 * 
	 * @GetMapping public List<RedisUser> getAllUsers() { return dao.findAll(); }
	 */
	
	@ApiOperation(value = "Bulk Private User Registration", tags = { "AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/create-bulk-user", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	@PreAuthorize(value = "hasRole('ADMIN')")
	public ResponseEntity<?> create(@Valid @RequestBody BulkPrivateUserCreationDTO userList, HttpServletRequest req,
			Device device) {
		return userService.createUsers(userList, req.getHeader("Authorization"), device);
	}
	
	@ApiOperation(value = "Get all users from Database", hidden = false, tags = { "USER SERVICE" })
	@RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, "application/hal+json" })
	public ResponseEntity<PagedModel<UserProfileResponsePojo>> getAllUsersDB(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		Page<Users> userPagedList = userService.getAllUsers(page, size);
		PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(userPagedList, userAssembler);
		return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
	}

	@ApiOperation(value = "Get User Details and Roles by ID from Redis (In-app use only)", tags = { "USER SERVICE" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/{id}")
	//    @Cacheable(key = "#id",value = "User")
	public ResponseEntity<?> findUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@ApiOperation(value = "Get User Details by Email (In-app use only)", tags = { "USER SERVICE" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("email/{email}")
	public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
		return userService.getUserByEmail(email);
	}

	@ApiOperation(value = "Get User Details by Phone (In-app use only)", tags = { "USER SERVICE" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@GetMapping("phone/{phone}")
	public ResponseEntity<?> getUserByPhone(@PathVariable String phone, HttpServletRequest req) {
		return userService.getUserByPhone(phone, req.getHeader("Authorization"));
	}

	@ApiOperation(value = "Phone Contact check  (Service consumption only. Do not Use)", tags = { "USER SERVICE" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/contact-check", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> contactCheck(@RequestBody ContactPojoReq contacts) {
		return userService.wayaContactCheck(contacts);
	}

	@ApiOperation(value = "Get my Info change", notes = "This endpointis used by logged in users to fetch their info", tags = {
			"USER SERVICE" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/myinfo")
	public ResponseEntity<?> getMyInfo() {
		return userService.getMyInfo();
	}

	@ApiOperation(value = "Delete User profiles", notes = "Disable user's accounts", tags = { "USER SERVICE" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> remove(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
		return userService.deleteUser(id, authorization);
	}

	@ApiOperation(value = "Get Users By role name count", notes = "This endpointis used to get all users by role name, it returns an integer of count", tags = {
			"USER SERVICE" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/count/{role}")
	public ResponseEntity<Integer> findAllUserByRoleCount(@PathVariable("role") String role) {
		return ResponseEntity.ok(userService.getUsersCount(role));
	}

	@ApiOperation(value = "Edit User Details", notes = "This endpointis used update user details", tags = {
			"USER SERVICE" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PutMapping("/update")
	public ResponseEntity<UserRoleUpdateRequest> updateUser(@RequestBody UserRoleUpdateRequest user) {

		return ResponseEntity.ok(userService.UpdateUser(user));
	}

	@ApiOperation(value = "Get user details for role service consumption", notes = "This endpointis used to get user details for Role Service", tags = {
			"USER SERVICE" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/get/user/for/role/{id}")
	public ResponseEntity<UserEditPojo> getUserForRole(@PathVariable("id") Long id) {
		return ResponseEntity.ok(userService.getUserForRole(id));
	}

}
