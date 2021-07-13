package com.waya.wayaauthenticationservice.service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;

public interface UserService {

	ResponseEntity<?> getUsers();

	ResponseEntity<?> getUsersByRole(long roleId);

	ResponseEntity<?> getUserByEmail(String email);

	ResponseEntity<?> getUserByPhone(String phone);

	ResponseEntity<?> getUserById(Long id);

	ResponseEntity<?> deleteUser(Long id);

	ResponseEntity<?> wayaContactCheck(ContactPojoReq contacts);

	ResponseEntity<?> getMyInfo();

	Integer getUsersCount(String roleName);

	UserRoleUpdateRequest UpdateUser(UserRoleUpdateRequest user);

	UserEditPojo UpdateUserDetails(UserEditPojo userEditPojo);

	// Get user details for Roles service
	UserEditPojo getUserForRole(Long id);


	ResponseEntity<?> isUserAdmin(Long userId);

	UserProfileResponsePojo toModelDTO(Users user);

	Page<Users> getAllUsers(int page, int size);

	ResponseEntity<?> getUserAndWalletByPhoneOrEmail(String phone);

	ResponseEntity<?> getUserAndWalletByUserId(Long id);

	ResponseEntity<?> createUsers(@Valid BulkCorporateUserCreationDTO userList, HttpServletRequest request, Device device);

	ResponseEntity<?> createUsers(@Valid BulkPrivateUserCreationDTO userList, HttpServletRequest request, Device device);
}
