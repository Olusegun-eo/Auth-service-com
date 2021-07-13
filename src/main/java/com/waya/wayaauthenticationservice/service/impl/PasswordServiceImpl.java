package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordResetContext;
import com.waya.wayaauthenticationservice.pojo.notification.DataPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NamesPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NotificationResponsePojo;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo2;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.ResponsePojo;

import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.MailService;
import com.waya.wayaauthenticationservice.service.PasswordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.util.Constant.VERIFY_RESET_TOKEN_MESSAGE;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final EmailService emailService;
    private final UserRepository usersRepo;
    private final ProfileRepository profileRepo;
    private final MailService mailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private AuthenticatedUserFacade authenticatedUserFacade;

    @Override
    public ResponseEntity<?> setForgotPassword(PasswordPojo passPojo) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(passPojo.getEmail()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + passPojo.getEmail(), null), HttpStatus.BAD_REQUEST);
            }
            boolean isPasswordMatched = passwordEncoder.matches(passPojo.getOldPassword(), user.getPassword());
            if (!isPasswordMatched) {
                return new ResponseEntity<>(new ErrorResponse("Incorrect Old Password"), HttpStatus.BAD_REQUEST);
            }
            String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
            user.setPassword(newPassword);
            user.setAccountStatus(1);
            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> sendPasswordResetOTP(String email, String baseUrl) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + email, null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            PasswordResetContext emailContext = new PasswordResetContext();
            Integer otpToken = generateOTP(email);
            emailContext.init(profile);
            emailContext.redirectTo(baseUrl);
            emailContext.seToken(String.valueOf(otpToken));
            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            return new ResponseEntity<>(new SuccessResponse("Email for Password Reset has been sent"), HttpStatus.OK);
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
    public ResponseEntity<?> resetPassword(ResetPasswordPojo passPojo) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(passPojo.getEmail()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + passPojo.getEmail(), null), HttpStatus.BAD_REQUEST);
            }
            EmailVerificationResponse emailVerificationResponse =
                    this.emailService.verifyEmailToken(passPojo.getEmail(), passPojo.getOtp());
            if(emailVerificationResponse == null || !emailVerificationResponse.isValid())
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NOT_VALID.getErrorMessage()
                        .replace("placeholder", "token: " + passPojo.getOtp())),
                        HttpStatus.BAD_REQUEST);

            String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
            user.setPassword(newPassword);
            user.setAccountStatus(1);

            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> forgotPin(String email) {
        try {

        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public ResponseEntity<?> changePin(PinPojo2 pinPojo) {
        Users user = usersRepo.findByEmailIgnoreCase(pinPojo.getEmail()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
        }
        boolean isPinMatched = passwordEncoder.matches(String.valueOf(pinPojo.getOldPin()), user.getPinHash());
        if (!isPinMatched) {
            return new ResponseEntity<>(new ErrorResponse("Incorrect Old Pin"), HttpStatus.BAD_REQUEST);
        }
        user.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getNewPin())));
        try {
            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> forgotPin(PinPojo pinPojo) {
        Users user = usersRepo.findByEmailIgnoreCase(pinPojo.getEmail()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.NOT_FOUND);
        }
        user.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getPin())));
        try {
            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Pin Changed.", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> createPin(PinPojo pinPojo) {
        try {
            // Check if email exists
            Users existingEmail = usersRepo.findById(pinPojo.getUserId()).orElse(null);

            if (existingEmail != null) {
                if (String.valueOf(pinPojo.getPin()).length() != 4) {
                    return new ResponseEntity<>(new ErrorResponse("Transaction pin should be exactly 4 Digits"),
                            HttpStatus.BAD_REQUEST);
                }
                existingEmail.setPinHash(passwordEncoder.encode(String.valueOf(pinPojo.getPin())));
                existingEmail.setPinCreated(true);
                usersRepo.save(existingEmail);
                return new ResponseEntity<>(new SuccessResponse("Transaction pin created successfully.", null),
                        HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(new ErrorResponse("This email does exists"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            return new ResponseEntity<>(new ErrorResponse("Error Occurred"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> validatePin(Long userId, int pin) {
        Users users = usersRepo.findById(userId).orElse(null);
        if (users == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
        boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
        if (isPinMatched) {
            return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> validatePinFromUser(int pin) {
        Users users = authenticatedUserFacade.getUser();
        if (users == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User."), HttpStatus.NOT_FOUND);
        }
        boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
        if (isPinMatched) {
            return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
    }

}
