package com.waya.wayaauthenticationservice.service;

import javax.servlet.http.HttpServletRequest;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.BaseUserPojo;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import com.waya.wayaauthenticationservice.pojo.UserPojo;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface AdminService {

    Page<Users> getCorporateUsers(boolean isCorporate, int page, int size);

    Page<Users> getUsersByRole(long roleId, int page, int size);

    ResponseEntity<?> createUser(BaseUserPojo userPojo, HttpServletRequest request, Device device);

    ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate, HttpServletRequest request, Device device);

    ByteArrayInputStream createExcelSheet(boolean isCorporate);



}
