package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordChangeEmailContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordResetContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.PinResetContext;
import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.MailService;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.*;
import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final OTPTokenService OTPTokenService;
    private final UserRepository usersRepo;
    private final ProfileRepository profileRepo;
    private final MailService mailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @Override
    public ResponseEntity<?> changePassword(PasswordPojo passPojo) {
        try {
            Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with input: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
            }
            boolean isPasswordMatched = passwordEncoder.matches(passPojo.getOldPassword(), user.getPassword());
            if (!isPasswordMatched) {
                return new ResponseEntity<>(new ErrorResponse("Incorrect Old Password"), HttpStatus.BAD_REQUEST);
            }

            Matcher matcher = emailPattern.matcher(passPojo.getPhoneOrEmail());
            boolean isEmail = matcher.matches();
            OTPRequestType otpRequestType = isEmail ? PASSWORD_CHANGE_EMAIL : PASSWORD_CHANGE_PHONE;

            Map<String, Object> map = doValidations(passPojo.getPhoneOrEmail(), String.valueOf(passPojo.getOtp()), isEmail, otpRequestType);

            boolean success = Boolean.valueOf(map.get("success").toString());
            if(!success){
                String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage()
                        .replace("placeholder", "token: " + passPojo.getOtp())
                        + "for: " + passPojo.getPhoneOrEmail() + ". Message is: "
                        + map.get("message").toString();
                return new ResponseEntity<>(new ErrorResponse(errorMessage),
                        HttpStatus.BAD_REQUEST);
            }
            String newPassword = passwordEncoder.encode(passPojo.getNewPassword());
            user.setPassword(newPassword);
            user.setCredentialsNonExpired(true);
            user.setAccountStatus(1);
            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("Password Changed.", null), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> sendPasswordChangeOTPByEmail(String email, String baseUrl) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + email, null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            PasswordChangeEmailContext emailContext = new PasswordChangeEmailContext();
            Integer otpToken = generateEmailOTP(email, PASSWORD_CHANGE_EMAIL);
            emailContext.init(profile);
            emailContext.redirectTo(baseUrl);
            emailContext.setToken(String.valueOf(otpToken));
            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            return new ResponseEntity<>(new SuccessResponse("Email for Password Reset has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPasswordChangeOTPByPhoneNumber(String phoneNumber) {
        try {
            Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with phoneNumber: " + phoneNumber, null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(phoneNumber, user.getName(), PASSWORD_CHANGE_PHONE));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordPojo passPojo) {
        try {
            Users user = usersRepo.findByEmailOrPhoneNumber(passPojo.getPhoneOrEmail()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email/phoneNumber: " + passPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);
            }
            Matcher matcher = emailPattern.matcher(passPojo.getPhoneOrEmail());
            boolean isEmail = matcher.matches();
            OTPRequestType otpRequestType = isEmail ? PASSWORD_RESET_EMAIL : PASSWORD_RESET_PHONE;

            Map<String, Object> map = doValidations(passPojo.getPhoneOrEmail(), String.valueOf(passPojo.getOtp()), isEmail, otpRequestType);

            if(!Boolean.valueOf(map.get("success").toString())){
                String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage()
                        .replace("placeholder", "token: " + passPojo.getOtp())
                        + "for: " + passPojo.getPhoneOrEmail() + ". Message is: "
                        + map.get("message").toString();
                return new ResponseEntity<>(new ErrorResponse(errorMessage),
                        HttpStatus.BAD_REQUEST);
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

    private Integer generateEmailOTP(String email, OTPRequestType otpRequestType) {
        OTPBase otpBase = this.OTPTokenService.generateEmailToken(email, otpRequestType);
        return otpBase.getCode();
    }

    @Override
    public ResponseEntity<?> sendPasswordResetOTPByEmail(String email, String baseUrl) {
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
            Integer otpToken = generateEmailOTP(email, PASSWORD_RESET_EMAIL);
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

    @Override
    public ResponseEntity<?> sendPasswordResetOTPByPhoneNumber(String phoneNumber) {
        try {
            Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with phoneNumber: " + phoneNumber, null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(phoneNumber, user.getName(), PASSWORD_RESET_PHONE));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPINResetOTPByPhoneNumber(String phoneNumber) {
        try {
            Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with phoneNumber: " + phoneNumber, null), HttpStatus.BAD_REQUEST);

            if(!user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                        HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(phoneNumber, user.getName(), PIN_RESET_PHONE));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPinResetOTPByEmail(String email, String redirectUrl) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + email, null), HttpStatus.BAD_REQUEST);

            if(!user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                        HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            PinResetContext emailContext = new PinResetContext();
            Integer otpToken = generateEmailOTP(email, PIN_RESET_EMAIL);
            emailContext.init(profile);
            emailContext.redirectTo(redirectUrl);
            emailContext.seToken(String.valueOf(otpToken));
            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            return new ResponseEntity<>(new SuccessResponse("Email for Pin Reset has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPINChangeOTPByPhoneNumber(String phoneNumber) {
        try {
            // Fetch Users Information
            Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with phoneNumber: " + phoneNumber, null), HttpStatus.BAD_REQUEST);

            if(!user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                        HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(phoneNumber, user.getName(), PIN_CHANGE_PHONE));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPinChangeOTPByEmail(String email, String redirectUrl) {
        try {
            // Fetch Users information by Email Address
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + email, null), HttpStatus.BAD_REQUEST);

            // Check if PIN has previously been created
            if(!user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse("Transaction PIN Not Setup yet"),
                        HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Build Mail Context to send Email to
            PinResetContext emailContext = new PinResetContext();
            Integer otpToken = generateEmailOTP(email, PIN_CHANGE_EMAIL);
            emailContext.init(profile);
            emailContext.redirectTo(redirectUrl);
            emailContext.seToken(String.valueOf(otpToken));

            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            return new ResponseEntity<>(new SuccessResponse("Email for Pin Reset has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> changeForgotPIN(NewPinPojo pinPojo) {
        try {
            if(pinPojo.getOtp().isBlank())
                return new ResponseEntity<>(new ErrorResponse("Kindly pass in a Pin"), HttpStatus.BAD_REQUEST);

            Users user = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + pinPojo.getPhoneOrEmail(), null), HttpStatus.BAD_REQUEST);

            if(!user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                        HttpStatus.BAD_REQUEST);

            Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
            boolean isEmail = matcher.matches();
            OTPRequestType otpRequestType = isEmail ? PIN_RESET_EMAIL : PIN_RESET_PHONE;

            Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail, otpRequestType);

            if(!Boolean.valueOf(map.get("success").toString())){
                String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage()
                        .replace("placeholder", "token: " + pinPojo.getOtp())
                        + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
                        + map.get("message").toString();
                return new ResponseEntity<>(new ErrorResponse(errorMessage),
                        HttpStatus.BAD_REQUEST);
            }
            String newPin = passwordEncoder.encode(pinPojo.getPin());
            user.setPinHash(newPin);
            usersRepo.save(user);
            return new ResponseEntity<>(new SuccessResponse("PIN Changed.", null), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> doValidations(String phoneOrEmail, String otp, boolean isEmail, OTPRequestType otpRequestType){
        String message;
        boolean success;
        Map<String, Object> map = new HashMap<>();
        OTPVerificationResponse otpResponse;
        if (isEmail) {
            otpResponse = this.OTPTokenService.verifyEmailToken(phoneOrEmail, Integer.parseInt(otp), otpRequestType);
        } else {
            otpResponse = this.OTPTokenService.verifySMSOTP(phoneOrEmail, Integer.parseInt(otp), otpRequestType);
        }
        success = otpResponse != null ? otpResponse.isValid() : false;
        message = otpResponse != null ? otpResponse.getMessage() : "Failure";
        map.put("success", success);
        map.put("message", message);
        return map;
    }

    @Override
    public ResponseEntity<?> changePin(ChangePINPojo pinPojo) {
        Users user = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Email"), HttpStatus.BAD_REQUEST);
        }
        if(!user.isPinCreated())
            return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                    HttpStatus.BAD_REQUEST);

        boolean isPinMatched = passwordEncoder.matches(String.valueOf(pinPojo.getOldPin()), user.getPinHash());
        if (!isPinMatched) {
            return new ResponseEntity<>(new ErrorResponse("Incorrect Old Pin"), HttpStatus.BAD_REQUEST);
        }

        Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
        boolean isEmail = matcher.matches();
        OTPRequestType otpRequestType = isEmail ? PIN_CHANGE_EMAIL : PIN_CHANGE_PHONE;

        Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail, otpRequestType);
        if(!Boolean.valueOf(map.get("success").toString())){
            String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage()
                    .replace("placeholder", "token: " + pinPojo.getOtp())
                    + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
                    + map.get("message").toString();
            return new ResponseEntity<>(new ErrorResponse(errorMessage),
                    HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<?> createPin(NewPinPojo pinPojo) {
        try {
            // Check if email exists
            Users users = usersRepo.findByEmailOrPhoneNumber(pinPojo.getPhoneOrEmail()).orElse(null);
            if (users != null) {
                if (users.isPinCreated())
                    return new ResponseEntity<>(new ErrorResponse("Transaction pin exists already"),
                            HttpStatus.BAD_REQUEST);

                Matcher matcher = emailPattern.matcher(pinPojo.getPhoneOrEmail());
                boolean isEmail = matcher.matches();
                OTPRequestType otpRequestType = isEmail ? PIN_CREATE_EMAIL : PIN_CREATE_PHONE;

                Map<String, Object> map = doValidations(pinPojo.getPhoneOrEmail(), pinPojo.getOtp(), isEmail, otpRequestType);
                if(!Boolean.valueOf(map.get("success").toString())){
                    String errorMessage = ErrorMessages.NOT_VALID.getErrorMessage()
                            .replace("placeholder", "token: " + pinPojo.getOtp())
                            + "for: " + pinPojo.getPhoneOrEmail() + ". Message is: "
                            + map.get("message").toString();
                    return new ResponseEntity<>(new ErrorResponse(errorMessage),
                            HttpStatus.BAD_REQUEST);
                }

                users.setPinHash(passwordEncoder.encode(pinPojo.getPin()));
                users.setPinCreated(true);
                usersRepo.save(users);

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
        Users users = usersRepo.findById(false, userId).orElse(null);
        if (users == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
        if(!users.isPinCreated())
            return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                    HttpStatus.BAD_REQUEST);

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
        if(!users.isPinCreated())
            return new ResponseEntity<>(new ErrorResponse("Transaction pin Not Setup yet"),
                    HttpStatus.BAD_REQUEST);

        boolean isPinMatched = passwordEncoder.matches(String.valueOf(pin), users.getPinHash());
        if (isPinMatched) {
            return new ResponseEntity<>(new SuccessResponse("Pin valid."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Invalid Pin."), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> sendPinCreationOTPPhone(String phoneNumber) {
        try {
            Users user = usersRepo.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with phoneNumber: " + phoneNumber, null), HttpStatus.BAD_REQUEST);

            if(user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()
                        + " For Pin", null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            // Send the Phone Number
            CompletableFuture.runAsync(() -> this.OTPTokenService.sendSMSOTP(phoneNumber, user.getName(), PIN_CREATE_PHONE));

            return new ResponseEntity<>(new SuccessResponse("OTP has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ResponseEntity<?> sendPinCreationOTPEmail(String email, String redirectUrl) {
        try {
            Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
            if (user == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For User with email: " + email, null), HttpStatus.BAD_REQUEST);

            if(user.isPinCreated())
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()
                        + " For Pin", null), HttpStatus.BAD_REQUEST);

            Profile profile = profileRepo.findByUserId(false, String.valueOf(user.getId())).orElse(null);
            if (profile == null)
                return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()
                        + " For Profile with userId: " + user.getId(), null), HttpStatus.BAD_REQUEST);

            PinResetContext emailContext = new PinResetContext();
            Integer otpToken = generateEmailOTP(email, PIN_CREATE_EMAIL);
            emailContext.init(profile);
            emailContext.redirectTo(redirectUrl);
            emailContext.seToken(String.valueOf(otpToken));
            // Send the Mail
            CompletableFuture.runAsync(() -> this.mailService.sendMail(emailContext));

            return new ResponseEntity<>(new SuccessResponse("Email for Pin Creation has been sent"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An Error Occurred: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

}
