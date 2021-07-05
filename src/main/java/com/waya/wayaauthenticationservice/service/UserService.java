package com.waya.wayaauthenticationservice.service;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;

public interface UserService {

	ResponseEntity<?> getUser(Long userId);

	ResponseEntity<?> getUsers();

	ResponseEntity<?> getUsersByRole(int roleId);

	ResponseEntity<?> getUserByEmail(String email);

	ResponseEntity<?> getUserByPhone(String phone, String token);

	ResponseEntity<?> getUserById(Long id);

	ResponseEntity<?> deleteUser(Long id, String token);

	ResponseEntity<?> wayaContactCheck(ContactPojoReq contacts);

	ResponseEntity<?> getMyInfo();

	Integer getUsersCount(String roleName);

	UserRoleUpdateRequest UpdateUser(UserRoleUpdateRequest user);

	// Get user details for Roles service
	UserEditPojo getUserForRole(Long id);

	public ResponseEntity<?> isUserAdmin(long userId);

	UserProfileResponsePojo toModelDTO(Users user);

	Page<Users> getAllUsers(int page, int size);

	ResponseEntity<?> createUsers(@Valid BulkPrivateUserCreationDTO userBulk, String requestToken, Device device);
}
