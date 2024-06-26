package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.SMSRequest;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.response.SMSResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface  AdminService {

    Page<Users> getCorporateUsers(boolean isCorporate, int page, int size);

    Page<Users> getUsersByRole(long roleId, int page, int size);

    ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device);

    ResponseEntity<?> createUser(@Valid CorporateUserPojo userPojo, HttpServletRequest request, Device device);

    ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate, HttpServletRequest request, Device device);

    ByteArrayInputStream createExcelSheet(boolean isCorporate);

    ResponseEntity<?> sendAdminOTP();

    ResponseEntity<?> verifyAdminOTP(Integer otp);

    ResponseEntity<?> manageUserRole(Long userId, boolean add, String roleName);

    ResponseEntity<?> manageUserPass(Long userId);

    InputStream createDeactivationExcelSheet();

    ResponseEntity<?> bulkDeactivation(MultipartFile file);

    ResponseEntity<?> bulkActivation(MultipartFile file);

    List<Role> getAllAuthRolesDB();

    ResponseEntity<?> toggleActivation(Long userId);

    ResponseEntity<?> toggleLock(Long userId);

    SMSResponse adminToggleSMSAlert(SMSRequest smsRequest);

    SMSResponse adminCheckSMSAlert(String phoneNumber);

    //ResponseEntity<?> createWayaUser(BaseUserPojo userPojo, HttpServletRequest request, Device device);
}
