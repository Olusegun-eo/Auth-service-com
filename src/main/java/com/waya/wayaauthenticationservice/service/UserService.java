package com.waya.wayaauthenticationservice.service;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.pojo.others.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.others.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.others.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.SuccessResponse;

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

	SuccessResponse UpdateUser(UserRoleUpdateRequest user);

	UserEditPojo UpdateUserDetails(UserEditPojo userEditPojo);

	// Get user details for Roles service
	UserEditPojo getUserForRole(Long id);

	ResponseEntity<?> activateAccount(Long userId);

	ResponseEntity<?> isUserAdmin(Long userId);

	ApiResponseBody<UserAccessResponse> getAccessResponse(Long userId);

	UserProfileResponsePojo toModelDTO(Users user);

	Page<Users> getAllUsers(int page, int size);

	ResponseEntity<?> getUserInfoByPhoneOrEmailForServiceConsumption(String phone);

	ResponseEntity<?> getUserInfoByUserIdForServiceConsumption(Long id);

	ResponseEntity<?> createUsers(@Valid BulkCorporateUserCreationDTO userList, HttpServletRequest request, Device device);

	ResponseEntity<?> createUsers(@Valid BulkPrivateUserCreationDTO userList, HttpServletRequest request, Device device);

    void deactivationServices(Users user, String token) throws InterruptedException, ExecutionException;

    ResponseEntity<?> unDeleteUser(Long id);

    ResponseEntity<?> deactivateAccounts(BulkPrivateUserCreationDTO excelToPrivateUserPojo);

    ResponseEntity<?> activateAccounts(BulkPrivateUserCreationDTO excelToPrivateUserPojo);

	ResponseEntity<?> validateWalletUserCall(Long userId, String key);

	Page<Users> getAllUsers(int page, int size, String searchString);

	ResponseEntity<?> validateUser();

	ResponseEntity<?> getUserSetupById(Long id);

	ResponseEntity<?> maintainUserSetup(UserSetupPojo pojo);
}
