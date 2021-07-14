package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;


@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;

    @Override
    public Page<Users> getCorporateUsers(boolean isCorporate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> usersSet = userRepository.findUserByIsCorporate(isCorporate, pageable);
        return usersSet;
    }

    @Override
    public Page<Users> getUsersByRole(long roleId, int page, int size) {
        Roles role = rolesRepository.findById(roleId).orElse(null);
        if (role == null) throw new
                CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), HttpStatus.BAD_REQUEST);
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> usersPage = userRepository.findByRolesListIn(Arrays.asList(role), pageable);
        return usersPage;
    }

    @Override
    public ResponseEntity<?> createUser(@Valid BaseUserPojo userPojo, HttpServletRequest request, Device device) {
        return authenticationService.createUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createUser(CorporateUserPojo userPojo, HttpServletRequest request, Device device) {
        return authenticationService.createCorporateUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate, HttpServletRequest request, Device device) {
        String message = "";

        if (ExcelHelper.hasExcelFormat(file)) {
            ResponseEntity<?> responseEntity;
            try {
                if (!isCorporate)
                    responseEntity = userService.createUsers(ExcelHelper.excelToPrivateUserPojo(file.getInputStream(),
                            file.getOriginalFilename()), request, device);
                else
                    responseEntity = userService.createUsers(ExcelHelper.excelToCorporatePojo(file.getInputStream(),
                            file.getOriginalFilename()), request, device);
                return responseEntity;
            } catch (Exception e) {
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        message = "Please upload an excel file!";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ByteArrayInputStream createExcelSheet(boolean isCorporate) {
        List<String> HEADERS = isCorporate ? ExcelHelper.CORPORATE_USER_HEADERS :
                ExcelHelper.PRIVATE_USER_HEADERS;
        ByteArrayInputStream in = ExcelHelper.createExcelSheet(HEADERS);
        return in;
    }


}
