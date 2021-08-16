package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.ADMIN_VERIFICATION;
import static com.waya.wayaauthenticationservice.util.HelperUtils.generateRandomPassword;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.AdminCheckContext;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AdminService;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.MessagingService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;

    @Autowired
    OTPTokenService otpTokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MessagingService messagingService;

    @Autowired
    AuthenticatedUserFacade authenticatedUserFacade;

    @Override
    public Page<Users> getCorporateUsers(boolean isCorporate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findUserByIsCorporate(isCorporate, pageable);
    }

    @Override
    public Page<Users> getUsersByRole(long roleId, int page, int size) {
        Role role = rolesRepository.findById(roleId).orElse(null);
        if (role == null) throw new
                CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), HttpStatus.BAD_REQUEST);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByRoleListIn(Collections.singletonList(role), pageable);
    }

    @Override
    public ResponseEntity<?> createUser(@Valid BaseUserPojo userPojo,
                                        HttpServletRequest request, Device device) {
        return authenticationService.createUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createUser(@Valid CorporateUserPojo userPojo,
                                        HttpServletRequest request, Device device) {
        return authenticationService.createCorporateUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate,
                                            HttpServletRequest request, Device device) {
        String message;
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
        return ExcelHelper.createExcelSheet(HEADERS);
    }

    @Override
    public ResponseEntity<?> sendAdminOTP() {
        try {
            Users adminUser = authenticatedUserFacade.getUser();
            if (adminUser == null)
                throw new CustomException("Error in Fetching Admin User", HttpStatus.INTERNAL_SERVER_ERROR);

            OTPBase otpToken = otpTokenService.generateOTP(adminUser.getPhoneNumber(), adminUser.getEmail(), ADMIN_VERIFICATION);

            //Send the Mail
            AdminCheckContext emailContext = new AdminCheckContext();
            emailContext.init(adminUser);
            emailContext.seToken(String.valueOf(otpToken.getCode()));
            CompletableFuture.runAsync(() -> this.messagingService.sendMail(emailContext));

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.otpTokenService.sendSMSOTP(adminUser.getName(), otpToken));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> verifyAdminOTP(Integer otp) {
        Users adminUser = authenticatedUserFacade.getUser();
        if (adminUser == null)
            throw new CustomException("Error in Fetching Admin User", HttpStatus.INTERNAL_SERVER_ERROR);

        OTPVerificationResponse resp = this.otpTokenService.verifyEmailToken(adminUser.getEmail(), otp, ADMIN_VERIFICATION);
        if (!resp.isValid()) {
            resp = this.otpTokenService.verifySMSOTP(adminUser.getPhoneNumber(), otp, ADMIN_VERIFICATION);
            this.otpTokenService.invalidateOldTokenViaEmail(adminUser.getEmail(), ADMIN_VERIFICATION);
        } else {
            this.otpTokenService.invalidateOldTokenViaPhoneNumber(adminUser.getPhoneNumber(), ADMIN_VERIFICATION);
        }

        if (!resp.isValid())
            return new ResponseEntity<>(new ErrorResponse("Error Validating OTP", resp), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(new SuccessResponse("OTP Verified Successfully.", resp), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> manageUserRole(Long userId, boolean add, String roleName) {
        try {
            Users user = userRepository.findById(false, userId).orElseThrow(() ->
                    new CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), HttpStatus.NOT_FOUND));

            Optional<Role> mRole = rolesRepository.findByName(roleName);
            if (mRole.isPresent()) {
                if (add) {
                    if (!user.getRoleList().contains(mRole.get()))
                        user.getRoleList().add(mRole.get());
                } else {
                    if (user.getRoleList().contains(mRole.get()))
                        user.getRoleList().remove(mRole.get());
                }
                userRepository.save(user);
            }
            return new ResponseEntity<>(new SuccessResponse("User Roles Updated", null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("An Error has Occurred :: {}", e.getMessage());
            throw new CustomException("An Error Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> manageUserPass(Long userId) {
        try {
            Users user = userRepository.findById(false, userId).orElseThrow(() ->
                    new CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), HttpStatus.NOT_FOUND));

            Users adminUser = authenticatedUserFacade.getUser();
            if (adminUser == null)
                throw new CustomException("Error in Fetching Admin User", HttpStatus.INTERNAL_SERVER_ERROR);
            String newPassword = generateRandomPassword();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setAccountStatus(-1);
            userRepository.save(user);

            CompletableFuture.runAsync(() -> this.authenticationService.sendNewPassword(newPassword, user));

            return new ResponseEntity<>(new SuccessResponse("User Password Reset Completed", null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("An Error has Occurred :: {}", e.getMessage());
            throw new CustomException("An Error Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public InputStream createDeactivationExcelSheet() {
        return ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_USER_HEADERS);
    }

    @Override
    public ResponseEntity<?> bulkDeactivation(MultipartFile file) {
        String message;
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                return userService.deactivateAccounts(ExcelHelper.excelToPrivateUserPojo(file.getInputStream(),
                            file.getOriginalFilename()));
            } catch (Exception e) {
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        message = "Please upload an excel file!";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> bulkActivation(MultipartFile file) {
        String message;
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                return userService.activateAccounts(ExcelHelper.excelToPrivateUserPojo(file.getInputStream(),
                        file.getOriginalFilename()));
            } catch (Exception e) {
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        message = "Please upload an excel file!";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @Override
    public List<Role> getAllAuthRolesDB() {
        return rolesRepository.findAll();
    }

}
