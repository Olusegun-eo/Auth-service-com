package com.waya.wayaauthenticationservice.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.SMSRequest;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.SMSResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.assembler.UserAssembler;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.ProfileService;
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
@PreAuthorize(value = "hasRole('APP_ADMIN')")
@Validated
public class AdminController {

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

	@Autowired
	ManageReferralService manageReferralService;

	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
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

	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/users/sort")
	public ResponseEntity<PagedModel<UserProfileResponsePojo>> getAllUsersDBSorted(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@RequestParam(value = "sort", defaultValue = "") String searchString) {
		Page<Users> userPagedList = userService.getAllUsers(page, size, searchString);
		PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(userPagedList,
				userAssembler);
		return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/all/users")
	public ResponseEntity<?> getAllUsersRec() {
		return userService.getAllUsersRec();
	}
	
	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping(value = "/all/users/sort-page", params = { "page", "size", "sortBy" })
	public ResponseEntity<?> findAllBySortAndPage(@RequestParam("page") final int page, @RequestParam("size") final int size,
			@RequestParam("sortBy") final String sortBy, @RequestParam("sortOrder") final String sortOrder) {

		PageRequest pageable = PageRequest.of(page, size, Direction.fromString(sortOrder), sortBy);

		//Page<UserProfilePojo> result = userRepository.findAll(pageable);
		return userService.GetAllUserProfile(pageable);

	}
	
	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping(value = "/all/users/page", params = { "page", "size" })
	public ResponseEntity<?> findAllByPage(@RequestParam("page") final int page, @RequestParam("size") final int size) {

		PageRequest pageable = PageRequest.of(page, size);

		//Page<UserProfilePojo> result = userRepository.findAll(pageable);
		return userService.GetAllUserProfile(pageable);
	}
	
	@ApiOperation(value = "Fetch all Users (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping(value = "/all/users/sort", params = { "sortBy" })
	public ResponseEntity<?> findAllBySort(@RequestParam("sortBy") final String sortBy,
			@RequestParam("sortOrder") final String sortOrder) {

		Sort sort = Sort.by(Direction.fromString(sortOrder), sortBy);

		return userService.GetAllUserProfile(sort);

	}

	@ApiOperation(value = "Create New Private User (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/users/create-private")
	public ResponseEntity<?> createNewPrivateUser(@Valid @RequestBody BaseUserPojo userPojo, HttpServletRequest request,
			Device device) {
		userPojo.setWayaAdmin(false);
		return adminService.createUser(userPojo, request, device);
	}

	@ApiOperation(value = "Create New Corporate User (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/users/create-corporate")
	public ResponseEntity<?> createNewCorporateUser(@Valid @RequestBody CorporateUserPojo userPojo,
			HttpServletRequest request, Device device) {
		return adminService.createUser(userPojo, request, device);
	}

	@ApiOperation(value = "Create New Waya Official User Account (Admin Endpoint). Only a user with Owner Role can execute", tags = {
			"ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/users/waya-account")
	@PreAuthorize(value = "hasAuthority('ROLE_OWNER_ADMIN')")
	public ResponseEntity<?> createWayaOfficialAccount(@Valid @RequestBody BaseUserPojo userPojo,
			HttpServletRequest request, Device device) {
		userPojo.setWayaAdmin(true);
		return adminService.createUser(userPojo, request, device);
	}

	@ApiOperation(value = "Bulk Private User Registration", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/users/bulk-user/private", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> createBulkPrivateUsers(@Valid @RequestBody BulkPrivateUserCreationDTO userList,
			HttpServletRequest request, Device device) {
		return userService.createUsers(userList, request, device);
	}

	@ApiOperation(value = "Bulk Corporate User Registration", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/users/bulk-user/corporate", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> createBulkCorporateUsers(@Valid @RequestBody BulkCorporateUserCreationDTO userList,
			HttpServletRequest request, Device device) {
		return userService.createUsers(userList, request, device);
	}

	@ApiOperation(value = "Bulk User Registration", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/users/bulk-user-excel/{isCorporate}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createBulkUserExcel(@RequestPart("file") MultipartFile file,
			@PathVariable(value = "isCorporate") boolean isCorporate, HttpServletRequest request, Device device) {
		return adminService.createBulkUser(file, isCorporate, request, device);
	}

	@ApiOperation(value = "Download Template for Bulk User Creation ", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/download/bulk-user-excel")
	public ResponseEntity<Resource> getFile(@RequestParam("isCorporate") boolean isCorporate) {
		String filename = "bulk-user-excel.xlsx";
		InputStreamResource file = new InputStreamResource(adminService.createExcelSheet(isCorporate));
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				// .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(file);
	}

	@ApiOperation(value = "Fetch Users by Roles (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/users/role/{roleId}")
	public ResponseEntity<?> getUsersByRole(@PathVariable int roleId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		Page<Users> usersPage = adminService.getUsersByRole(roleId, page, size);
		PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(usersPage, userAssembler);
		return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
	}

	@ApiOperation(value = "Fetch Users By Corporate/Non-Corporate (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/users/corporate/{isCorporate}")
	public ResponseEntity<?> findAllCorporateUsers(@PathVariable("isCorporate") boolean isCorporate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		Page<Users> usersPage = adminService.getCorporateUsers(isCorporate, page, size);
		PagedModel<UserProfileResponsePojo> userPagedModel = pagedResourcesAssembler.toModel(usersPage, userAssembler);
		return new ResponseEntity<>(userPagedModel, HttpStatus.OK);
	}

	@ApiOperation(value = "Admin Update Corporate Profile of a User", notes = "Admin Should be able to update Corporate Profile on behalf of the user", tags = {
			"ADMIN" })
	@PutMapping("/update-corporate-profile/{userId}")
	ResponseEntity<?> updateCorporateProfile(
			@Valid @RequestBody UpdateCorporateProfileRequest updateCorporateProfileRequest,
			@PathVariable String userId) {
		UserProfileResponse corporateProfileResponse = profileService.updateProfile(updateCorporateProfileRequest,
				userId);
		var response = new com.waya.wayaauthenticationservice.response.ApiResponseBody<>(corporateProfileResponse,
				"profile updated successfully", true);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Admin Update Personal Profile of a User", notes = "Admin Should be able to update Profile Profile on behalf of the user", tags = {
			"ADMIN" })
	@PutMapping("/update-personal-profile/{userId}")
	ResponseEntity<?> updateProfile(@Valid @RequestBody UpdatePersonalProfileRequest updatePersonalProfileRequest,
			@PathVariable String userId) {
		UserProfileResponse profileResponse = profileService.updateProfile(updatePersonalProfileRequest, userId);
		return new ResponseEntity<>(new com.waya.wayaauthenticationservice.response.ApiResponseBody<>(profileResponse,
				"profile updated successfully", true), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Send OTP to email and phoneNumber for Waya Account Viewing", notes = "Send OTP to email and phoneNumber for Waya Account Viewing", tags = {
			"ADMIN" })
	@GetMapping("/authenticate-waya/otp-send")
	public ResponseEntity<?> sendAdminOTP() {
		return adminService.sendAdminOTP();
	}

	@ApiOperation(value = "Send OTP to email and phoneNumber for Waya Account Viewing", notes = "Send OTP to email and phoneNumber for Waya Account Viewing", tags = {
			"ADMIN" })
	@PostMapping("/authenticate-waya/otp-verify/{otp}")
	public ResponseEntity<?> verifyAdminOTP(@PathVariable Integer otp) {
		return adminService.verifyAdminOTP(otp);
	}

	@ApiOperation(value = "Manage and Reset Users Password", notes = "To Alter Password of a User", tags = { "ADMIN" })
	@PostMapping("/reset/{userId}/password")
	public ResponseEntity<?> manageUserPass(@PathVariable Long userId) {
		return adminService.manageUserPass(userId);
	}

	@ApiOperation(value = "Download Template for Bulk User Activation/Deactivation ", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/bulk/account-activation/bulk-user-excel")
	public ResponseEntity<Resource> getFile() {
		String filename = "bulk-user-excel.xlsx";
		InputStreamResource file = new InputStreamResource(adminService.createDeactivationExcelSheet());
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				// .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(file);
	}

	@ApiOperation(value = "Deactivate Users via Excel Upload", notes = "To deactivate bulk accounts via excel upload", tags = {
			"ADMIN" })
	@PostMapping(path = "bulk/account-deactivation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> bulkDeactivation(@RequestPart("file") MultipartFile file) {
		return adminService.bulkDeactivation(file);
	}

	@ApiOperation(value = "Re-activate Users via Excel Upload", notes = "To reactivate bulk accounts via excel upload", tags = {
			"ADMIN" })
	@PostMapping(path = "bulk/account-activation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> bulkActivation(@RequestPart("file") MultipartFile file) {
		return adminService.bulkActivation(file);
	}

	@ApiOperation(value = "To Toggle Activation and Deactivation of a single User", notes = "To Toggle Activation and Deactivation of a single User", tags = {
			"ADMIN" })
	@PostMapping("/users/activation-toggle")
	public ResponseEntity<?> toggleActivation(@RequestParam("userId") Long userId) {
		return adminService.toggleActivation(userId);
	}

	@ApiOperation(value = "To Toggle User Lock and Unlock Actions of a single User", notes = "To Toggle User Lock and Unlock Actions of a single User", tags = {
			"ADMIN" })
	@PostMapping("/users/lock-toggle/un-lock")
	public ResponseEntity<?> toggleLock(@RequestParam("userId") Long userId) {
		return adminService.toggleLock(userId);
	}

	@ApiOperation(value = "Fetch all Auth Users Roles (Admin Endpoint)", tags = { "ADMIN" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/manage-user/roles")
	public ResponseEntity<List<Role>> getAllAuthRolesDB() {
		return ResponseEntity.ok(adminService.getAllAuthRolesDB());
	}

	@ApiOperation(value = "Manage Users Role and Permissions", notes = "To Alter roles and Permission from a User. Only a user with an Higher role can upgrade, "
			+ "except when another Owner intends upgrading a user role to Owner", tags = { "ADMIN" })
	@PostMapping("/manage-user/{userId}/roles/{add}/{roleName}")
	@PreAuthorize(value = "@userSecurity.useHierarchyForRoles(#roleName, authentication)")
	public ResponseEntity<?> manageRoles(@PathVariable Long userId, @PathVariable boolean add,
			@PathVariable String roleName) {

		return adminService.manageUserRole(userId, add, roleName);
	}


	@ApiOperation(value = "SMS Alert Subscription on behalf of the user (Admin Endpoint)", notes = "", tags = { "ADMIN" })
	@PostMapping("/users/subscribe-sms-alert")
	public ResponseEntity<ApiResponseBody<SMSResponse>> adminToggleSMSAlert(@RequestBody SMSRequest smsRequest) {
		SMSResponse SMSResponse = adminService.adminToggleSMSAlert(smsRequest);
		ApiResponseBody<SMSResponse> response = new ApiResponseBody<>(SMSResponse, "Done successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "SMS Alert Status on behalf of the user (Admin Endpoint)", notes = "", tags = { "ADMIN" })
	@GetMapping("/users/check-sms-alert-status/{phoneNumber}")
	public ResponseEntity<ApiResponseBody<SMSResponse>> adminCheckSMSAlert(@Valid @ApiParam(example = "2348054354344") @PathVariable @ValidPhone String phoneNumber) {
		SMSResponse SMSResponse = adminService.adminCheckSMSAlert(phoneNumber);
		ApiResponseBody<SMSResponse> response = new ApiResponseBody<>(SMSResponse, "Done successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = " Get All users SMS alert status. (Admin Endpoint)",notes = "", tags = {"ADMIN"})
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
			@io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
	})
	@GetMapping("/users/get-all-users-sms-alert-status")
	ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersSMSAlertStatus(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) throws CustomException {
		Map<String, Object> map = manageReferralService.getUsersSMSAlertStatus(page,size);
		ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(map, "Data retrieved successfully", true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}





}
