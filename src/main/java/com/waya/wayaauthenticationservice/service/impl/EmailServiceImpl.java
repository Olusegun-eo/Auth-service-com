package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.AccountVerificationEmailContext;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.MailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final OTPRepository otpRepository;
    private final MailService mailService;

    /**
     * generates a 6 digit OTP code and send code to email topic
     * in kafka
     *
     * @param baseUrl url to redirect to
     * @param profile userProfile
     */
    @Override
    public boolean sendAcctVerificationEmailToken(String baseUrl, Profile profile, OTPRequestType otpRequestType) {
        try {
            //generate the token
            OTPBase otp = generateEmailToken(profile.getEmail(), otpRequestType);
            AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
            emailContext.init(profile);
            emailContext.buildURL(baseUrl);
            emailContext.setToken(String.valueOf(otp.getCode()));
            try {
                mailService.sendMail(emailContext);
            } catch (Exception e) {
                log.error("An Error Occurred:: {}", e.getMessage());
            }
            // mailService.sendMail(user.getEmail(), message);
            log.info("Activation email sent!! \n");
            return true;
        } catch (Exception exception) {
            log.error("could not process data ", exception);
        }
        return false;
    }

    /**
     * verify Token and then cache the result
     *
     * @param email phone number
     * @param otp   otp
     * @return OTPVerificationResponse
     */
    //@CachePut(cacheNames = "OTPBase", key = "#email")
    @Override
    public OTPVerificationResponse verifyEmailToken(String email, Integer otp, OTPRequestType otpRequestType) {
        try {
            Optional<OTPBase> otpBase = otpRepository.getOtpDetailsViaEmail(email, otp, String.valueOf(otpRequestType));
            if (otpBase.isPresent()) {
                OTPBase token = otpBase.get();
                if (token.isValid()) {
                    LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(2);
                    otpRepository.updateTokenForEmail(email, token.getId(), newTokenExpiryDate, false, String.valueOf(otpRequestType));
                    return new OTPVerificationResponse(true, OTP_SUCCESS_MESSAGE);
                } else {
                    return new OTPVerificationResponse(false, EMAIL_VERIFICATION_MSG_ERROR);
                }
            }
            return new OTPVerificationResponse(false, ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        } catch (Exception exception) {
            log.error("could not process data ", exception);
            throw new CustomException("Invalid Token", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * generate an otp for email, saves the record in the OTP table
     * and invalidates the previous record
     *
     * @param email user email
     * @return OTPBase
     */
    @Override
    public OTPBase generateEmailToken(String email, OTPRequestType otpRequestType) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setEmail(email);
        otp.setValid(true);
        otp.setRequestType(otpRequestType);
        otp.setExpiryDate(120);

        //update previous otp expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(12);
        //invalidate the previous record
        otpRepository.invalidatePreviousRecordsViaEmail(email, newExpiryDate, false, String.valueOf(otpRequestType));
        otp = otpRepository.save(otp);
        return otp;
    }

    @Override
    public void invalidateOldToken(String email, OTPRequestType otpRequestType){
        //update previous otp expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(12);
        otpRepository.invalidatePreviousRecordsViaEmail(email, newExpiryDate, false, String.valueOf(otpRequestType));
    }

}
