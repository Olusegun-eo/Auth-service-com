package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import com.waya.wayaauthenticationservice.streams.RecipientsSMS;
import com.waya.wayaauthenticationservice.streams.StreamDataSMS;
import com.waya.wayaauthenticationservice.streams.StreamPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

@Service
@Slf4j
@AllArgsConstructor
public class SMSTokenServiceImpl implements SMSTokenService {

    private final OTPRepository otpRepository;
    private final MessageQueueProducer messageQueueProducer;

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

}
