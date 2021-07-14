package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.util.Constant.EMAIL_VERIFICATION_MSG;
import static com.waya.wayaauthenticationservice.util.Constant.EMAIL_VERIFICATION_MSG_ERROR;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Email;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.AccountVerificationEmailContext;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.MailService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final OTPRepository otpRepository;
    private final ProfileRepository profileRepository;
    private final MailService mailService;

    /**
     * generates a 6 digit OTP code and send code to email topic
     * in kafka
     *
     * @param email    request
     */
    @Override
    public boolean sendAcctVerificationEmailToken(String baseUrl, @Valid @Email String email) {
        try {
            Profile profile = profileRepository.findByEmail(false, email)
                    .orElseThrow(() -> new CustomException("profile does not exist", HttpStatus.NOT_FOUND));
            //generate the token
            OTPBase otp = generateEmailToken(profile.getEmail());
            AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
            emailContext.init(profile);
            emailContext.buildURL(baseUrl, email, String.valueOf(otp.getCode()));
            emailContext.setToken( String.valueOf(otp.getCode()));
            try{
                mailService.sendMail(emailContext);
            }catch(Exception e){
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
    public EmailVerificationResponse verifyEmailToken(String email, Integer otp) {
        try {
            Optional<OTPBase> otpBase = otpRepository.getOtpDetailsViaEmail(email, otp);
            if (otpBase.isPresent()) {
                OTPBase token = otpBase.get();
                if (token.isValid()) {
                    LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(2);
                    otpRepository.updateTokenForEmail(email, token.getId(), newTokenExpiryDate, false);
                    return new EmailVerificationResponse(true, EMAIL_VERIFICATION_MSG);
                } else {
                    return new EmailVerificationResponse(false, EMAIL_VERIFICATION_MSG_ERROR);
                }
            }
            return new EmailVerificationResponse(false, ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

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
    public OTPBase generateEmailToken(String email) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setEmail(email);
        otp.setExpiryDate(120);

        //update previous otp expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(12);
        //invalidate the previous record
        otpRepository.invalidatePreviousRecordsViaEmail(email, newExpiryDate, false);
        otp = otpRepository.save(otp);
        return otp;
    }

}
