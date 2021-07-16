package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.util.Constant.INVALID_OTP;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_2;
import static com.waya.wayaauthenticationservice.util.Constant.OTP_ERROR_MESSAGE;
import static com.waya.wayaauthenticationservice.util.Constant.OTP_SUCCESS_MESSAGE;
import static com.waya.wayaauthenticationservice.util.Constant.SMS_TOPIC;
import static com.waya.wayaauthenticationservice.util.Constant.TWILIO_PROVIDER;
import static com.waya.wayaauthenticationservice.util.Constant.WAYAPAY;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import com.waya.wayaauthenticationservice.streams.RecipientsSMS;
import com.waya.wayaauthenticationservice.streams.StreamDataSMS;
import com.waya.wayaauthenticationservice.streams.StreamPayload;

@Component
public class SMSTokenServiceImpl implements SMSTokenService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final OTPRepository otpRepository;
    private final MessageQueueProducer messageQueueProducer;

    @Autowired
    public SMSTokenServiceImpl(OTPRepository otpRepository,
                               MessageQueueProducer messageQueueProducer,
                               ProfileRepository profileRepository, Gson gson
    ) {
        this.otpRepository = otpRepository;
        this.messageQueueProducer = messageQueueProducer;
    }

    /**
     * generate otp for sms
     *
     * @param phoneNumber user phone number
     * @return OTPBase
     */
    @Override
    public OTPBase generateSMSOTP(String phoneNumber) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setPhoneNumber(phoneNumber);
        otp.setValid(true);
        otp.setExpiryDate(10);

        //update previous token expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(1);

        otpRepository.invalidatePreviousRecords(phoneNumber, newExpiryDate, false);
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
    public boolean sendSMSOTP(String phoneNumber, String name) {
        try {
            OTPBase otp = generateSMSOTP(phoneNumber);

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
    public ApiResponse<OTPVerificationResponse> verifySMSOTP(String phoneNumber, Integer otp) {
        try {
            Optional<OTPBase> otpBase = otpRepository.getOtpDetailsViaPhoneNumber(phoneNumber, otp);
            if (otpBase.isPresent()) {
                OTPBase token = otpBase.get();
                LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(1);
                if (token.isValid()) {
                    otpRepository.updateToken(phoneNumber, token.getId(), newTokenExpiryDate, false);
                    return new ApiResponse<>(new OTPVerificationResponse(true,
                            OTP_SUCCESS_MESSAGE), OTP_SUCCESS_MESSAGE, true);
                } else {
                    return new ApiResponse<>(new OTPVerificationResponse(false,
                            OTP_ERROR_MESSAGE), OTP_ERROR_MESSAGE, false);
                }
            }
            return new ApiResponse<>(new OTPVerificationResponse(false,
                    INVALID_OTP), INVALID_OTP, false);
        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
            return new ApiResponse<>(new OTPVerificationResponse(false,
                    INVALID_OTP), INVALID_OTP, false);
        }
    }

}
