package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import com.waya.wayaauthenticationservice.response.notification.DataPojo;
import com.waya.wayaauthenticationservice.response.notification.NamesPojo;
import com.waya.wayaauthenticationservice.response.notification.NotificationResponsePojo;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.util.Constant.VERIFY_RESET_TOKEN_MESSAGE;

@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final NotificationProxy notificationProxy;
    private final OTPRepository otpRepository;
    private final EmailService emailService;
    private final UserRepository usersRepo;

    public PasswordServiceImpl(NotificationProxy notificationProxy, OTPRepository otpRepository,
                               EmailService emailService, UserRepository usersRepo) {
        this.notificationProxy = notificationProxy;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.usersRepo = usersRepo;
    }

    @Override
    public ResponsePojo setForgotPassword(PasswordPojo passwordPojo) {
        try {

        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public ResponsePojo sendOtp(String email) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return ResponsePojo.response(false, ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), HttpStatus.BAD_REQUEST.value());

            NotificationResponsePojo notification = new NotificationResponsePojo();
            NamesPojo name = new NamesPojo();
            name.setEmail(email);
            name.setFullName(user.getName());
            List<NamesPojo> names = new ArrayList<>();
            names.add(name);
            DataPojo dataPojo = new DataPojo();

            Integer otp = generateOTP(email);

            dataPojo.setMessage(VERIFY_RESET_TOKEN_MESSAGE + otp);
            dataPojo.setNames(names);
            notification.setData(dataPojo);
            notification.setEventType("EMAIL");
            notification.setInitiator(email);

            CompletableFuture.runAsync(() -> notificationProxy.sendEmail(notification));

            return ResponsePojo.response(true, "Email has been sent", HttpStatus.OK.value());
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Integer generateOTP(String email) {
        OTPBase otpBase = this.emailService.generateEmailToken(email);
        return otpBase.getCode();
    }

    @Override
    public ResponsePojo resetPassword(ResetPasswordPojo resetPasswordPojo) {
        try {

        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public ResponsePojo forgotPin(String email) {
        try {

        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public ResponsePojo verifyEmail(String email) {
        return null;
    }
}
