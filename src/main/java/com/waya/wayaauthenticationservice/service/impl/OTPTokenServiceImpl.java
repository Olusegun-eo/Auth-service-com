package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;
import com.waya.wayaauthenticationservice.pojo.mail.context.AccountVerificationEmailContext;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.MailService;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.streams.RecipientsSMS;
import com.waya.wayaauthenticationservice.streams.StreamDataSMS;
import com.waya.wayaauthenticationservice.streams.StreamPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

@Service
@Slf4j
@AllArgsConstructor
public class OTPTokenServiceImpl implements OTPTokenService {

    private final OTPRepository otpRepository;
    private final MessageQueueProducer messageQueueProducer;
    private final MailService mailService;

    /**
     * generates a 6 digit OTP code and send code to email topic
     * in kafka
     *
     * @param baseUrl url to redirect to
     * @param profile userProfile
     */
    @Override
    public boolean sendVerificationEmailToken(String baseUrl, Profile profile, OTPRequestType otpRequestType) {
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
     * generates a 6 digit OTP code and send code to email topic
     * in kafka
     *
     * @param emailContext userProfile
     */
    @Override
    public boolean sendEmailToken(AbstractEmailContext emailContext) {
        try {
            try {
                mailService.sendMail(emailContext);
            } catch (Exception e) {
                log.error("An Error Occurred:: {}", e.getMessage());
            }
            log.info(" email sent!! \n");
            return true;
        } catch (Exception exception) {
            log.error("could not process data ", exception);
        }
        return false;
    }

    @Override
    public OTPVerificationResponse verifyJointOTP(String emailOrPhoneNumber, String otp, OTPRequestType otpRequestType) {
        try {
            Optional<OTPBase> otpBase = otpRepository.getOtpDetailsViaEmailOrPhoneNumber(emailOrPhoneNumber, Integer.valueOf(otp), String.valueOf(otpRequestType));
            if (otpBase.isPresent()) {
                OTPBase token = otpBase.get();
                if (token.isValid()) {
                    LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(2);
                    otpRepository.updateTokenForJoint(token.getPhoneNumber(), token.getId(),
                            token.getEmail(), newTokenExpiryDate, false,
                            String.valueOf(otpRequestType));

                    return new OTPVerificationResponse(true, OTP_SUCCESS_MESSAGE);
                } else {
                    return new OTPVerificationResponse(false, INVALID_OTP);
                }
            }
            return new OTPVerificationResponse(false, ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        } catch (Exception exception) {
            log.error("could not process data ", exception);
            throw new CustomException("Invalid Token", HttpStatus.UNPROCESSABLE_ENTITY);
        }
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

    /**
     * generate otp for sms
     *
     * @param phoneNumber user phone number
     * @return OTPBase
     */
    @Override
    public OTPBase generateSMSOTP(String phoneNumber, OTPRequestType otpRequestType) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setPhoneNumber(phoneNumber);
        otp.setValid(true);
        otp.setRequestType(otpRequestType);
        otp.setExpiryDate(10);

        //update previous token expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(1);

        otpRepository.invalidatePreviousRecords(phoneNumber, newExpiryDate, false, String.valueOf(otpRequestType));
        otpRepository.save(otp);
        return otp;
    }

    /**
     * generate otp for both identification means
     *
     * @param phoneNumber user phone number
     * @param email user email address
     * @return OTPBase
     */
    @Override
    public OTPBase generateOTP(String phoneNumber, String email, OTPRequestType otpRequestType) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setPhoneNumber(phoneNumber);
        otp.setEmail(email);
        otp.setValid(true);
        otp.setRequestType(otpRequestType);
        otp.setExpiryDate(10);

        //update previous token expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(1);

        otpRepository.invalidatePreviousRecords(phoneNumber, email, newExpiryDate,
                false, String.valueOf(otpRequestType));
        otpRepository.save(otp);
        return otp;
    }

    /**
     * generates a 6 digit OTP code and send code to sms topic
     * in kafka
     * @param name        user Name
     * @param otp OTPBase Object
     */
    @Override
    public boolean sendSMSOTP(String name, OTPBase otp) {
        try {
            StreamPayload<StreamDataSMS> post = new StreamPayload<>();
            post.setEventType(StreamsEventType.SMS.toString());
            post.setInitiator(WAYAPAY);
            post.setToken(null);
            post.setKey(TWILIO_PROVIDER);

            StreamDataSMS data = new StreamDataSMS();
            data.setMessage(MESSAGE + otp.getCode() + MESSAGE_2);
            data.setRecipients(Collections.singletonList(new RecipientsSMS(name, "+".concat(otp.getPhoneNumber()))));

            post.setData(data);

            messageQueueProducer.send(SMS_TOPIC, post);
            log.info("otp sent to kafka message queue::: {}", post);
            return true;
        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
        }
        return false;
    }

    /**
     * generates a 6 digit OTP code and send code to sms topic
     * in kafka
     *
     * @param phoneNumber phone number
     * @param name        user Name
     */
    @Override
    public boolean sendSMSOTP(String phoneNumber, String name, OTPRequestType otpRequestType) {
        try {
            OTPBase otp = generateSMSOTP(phoneNumber, otpRequestType);

            StreamPayload<StreamDataSMS> post = new StreamPayload<>();
            post.setEventType(StreamsEventType.SMS.toString());
            post.setInitiator(WAYAPAY);
            post.setToken(null);
            post.setKey(TWILIO_PROVIDER);

            StreamDataSMS data = new StreamDataSMS();
            data.setMessage(MESSAGE + otp.getCode() + MESSAGE_2);
            data.setRecipients(Collections.singletonList(new RecipientsSMS(name, "+".concat(otp.getPhoneNumber()))));

            post.setData(data);

            messageQueueProducer.send(SMS_TOPIC, post);
            log.info("otp sent to kafka message queue::: {}", post);
            return true;

        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
        }
        return false;
    }

    /**
     * verify opt and cache result
     *
     * @param phoneNumber phone number
     * @param otp         otp
     * @return OTPVerificationResponse
     */
    //@CachePut(cacheNames = "OTPBase", key = "#phoneNumber")
    @Override
    public OTPVerificationResponse verifySMSOTP(String phoneNumber, Integer otp, OTPRequestType otpRequestType) {
        try {
            Optional<OTPBase> otpBase = otpRepository.getOtpDetailsViaPhoneNumber(phoneNumber, otp, String.valueOf(otpRequestType));
            if (otpBase.isPresent()) {
                OTPBase token = otpBase.get();
                LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(1);
                if (token.isValid()) {
                    otpRepository.updateToken(phoneNumber, token.getId(), newTokenExpiryDate, false, String.valueOf(otpRequestType));
                    return new OTPVerificationResponse(true,
                            OTP_SUCCESS_MESSAGE);
                } else {
                    return new OTPVerificationResponse(false,
                            OTP_ERROR_MESSAGE);
                }
            }
            return new OTPVerificationResponse(false,
                    INVALID_OTP);
        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
            return new OTPVerificationResponse(false, INVALID_OTP);
        }
    }

    /**
     * verify opt and cache result
     *
     * @param phoneNumber phone number
     * @param otpRequestType  OTPRequestType
     * @return void
     */
    @Override
    public void invalidateOldTokenViaPhoneNumber(String phoneNumber, OTPRequestType otpRequestType){
        //update previous token expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(1);
        otpRepository.invalidatePreviousRecords(phoneNumber, newExpiryDate, false, String.valueOf(otpRequestType));
    }

    @Override
    public void invalidateOldTokenViaEmail(String email, OTPRequestType otpRequestType){
        //update previous otp expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(12);
        otpRepository.invalidatePreviousRecordsViaEmail(email, newExpiryDate, false, String.valueOf(otpRequestType));
    }

    @Override
    public void sendAccountVerificationToken(Profile profile, OTPRequestType otpRequestType, String baseUrl) {
        try{
            OTPBase otp = generateOTP(profile.getPhoneNumber(), profile.getEmail(), otpRequestType);

            String fullName = String.format("%s %s", profile.getFirstName(),
                    profile.getSurname());

            // Send SMS
            sendSMSOTP(fullName, otp);

            // Send Email
            AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
            emailContext.init(profile);
            emailContext.setToken(String.valueOf(otp.getCode()));
            emailContext.buildURL(baseUrl);
            sendEmailToken(emailContext);
        }catch(Exception ex){
            log.error("An Error Occurred :: {}", ex.getMessage());
        }
    }


}