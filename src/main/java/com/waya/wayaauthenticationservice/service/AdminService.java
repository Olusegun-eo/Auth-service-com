package com.waya.wayaauthenticationservice.service;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;

public interface AdminService {

    Page<Users> getCorporateUsers(boolean isCorporate, int page, int size);

    Page<Users> getUsersByRole(long roleId, int page, int size);

    ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device);

    ResponseEntity<?> createUser(@Valid CorporateUserPojo userPojo, HttpServletRequest request, Device device);

    ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate, HttpServletRequest request, Device device);

    ByteArrayInputStream createExcelSheet(boolean isCorporate);

}
