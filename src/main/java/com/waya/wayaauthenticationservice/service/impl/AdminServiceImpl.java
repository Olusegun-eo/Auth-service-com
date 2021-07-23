package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
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
import com.waya.wayaauthenticationservice.service.*;
import com.waya.wayaauthenticationservice.util.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.ADMIN_VERIFICATION;


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
    EmailService emailService;

    @Autowired
    SMSTokenService smsTokenService;

    @Autowired
    MailService mailService;

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
    public ResponseEntity<?> createUser(@Valid BaseUserPojo userPojo, HttpServletRequest request, Device device) {
        return authenticationService.createUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createUser(@Valid CorporateUserPojo userPojo, HttpServletRequest request, Device device) {
        return authenticationService.createCorporateUser(userPojo, request, device, true);
    }

    @Override
    public ResponseEntity<?> createBulkUser(MultipartFile file, boolean isCorporate, HttpServletRequest request, Device device) {
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

            Profile profile = profileRepo.findByUserId(false, String.valueOf(adminUser.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + adminUser.getId(), null), HttpStatus.BAD_REQUEST);

            AdminCheckContext emailContext = new AdminCheckContext();
            Integer otpToken = generateEmailOTP(adminUser.getEmail(), ADMIN_VERIFICATION);
            emailContext.init(profile);
            emailContext.seToken(String.valueOf(otpToken));

            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.smsTokenService.sendSMSOTP(adminUser.getPhoneNumber(), adminUser.getName(), ADMIN_VERIFICATION));

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

        OTPVerificationResponse resp = this.emailService.verifyEmailToken(adminUser.getEmail(), otp, ADMIN_VERIFICATION);
        if (!resp.isValid()) {
            resp = this.smsTokenService.verifySMSOTP(adminUser.getPhoneNumber(), otp, ADMIN_VERIFICATION);
            this.emailService.invalidateOldToken(adminUser.getEmail(), ADMIN_VERIFICATION);
        } else {
            this.smsTokenService.invalidateOldToken(adminUser.getPhoneNumber(), ADMIN_VERIFICATION);
        }

        if (!resp.isValid())
            return new ResponseEntity<>(new ErrorResponse("Error Validating OTP", resp), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(new SuccessResponse("OTP Verified Successfully.", resp), HttpStatus.OK);
    }

    private Integer generateEmailOTP(String email, OTPRequestType otpRequestType) {
        OTPBase otpBase = this.emailService.generateEmailToken(email, otpRequestType);
        return otpBase.getCode();
    }

}
