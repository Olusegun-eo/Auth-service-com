package com.waya.wayaauthenticationservice.service.impl;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import com.waya.wayaauthenticationservice.streams.*;
import com.waya.wayaauthenticationservice.util.profile.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

@Component
public class SMSTokenServiceImpl implements SMSTokenService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final OTPRepository otpRepository;
    private final MessageQueueProducer messageQueueProducer;
    private final ProfileRepository profileRepository;
    private final String welcomeMessage;

    private final Gson gson;

    @Autowired
    public SMSTokenServiceImpl(OTPRepository otpRepository,
                               MessageQueueProducer messageQueueProducer,
                               ProfileRepository profileRepository, Gson gson,
                               @Value("${welcome-email.message}") String welcomeMessage
    ) {
        this.otpRepository = otpRepository;
        this.messageQueueProducer = messageQueueProducer;
        this.profileRepository = profileRepository;
        this.gson = gson;
        this.welcomeMessage = welcomeMessage;
    }

    /**
     * generate otp for sms
     *
     * @param phoneNumber user phone number
     * @param email       user email
     * @return OTPBase
     */
    private OTPBase generateSMSOTP(String phoneNumber, String email) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setPhoneNumber(phoneNumber);
        otp.setEmail(email);
        otp.setExpiryDate(10);
        otp.setValid(otp.isValid());

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
     * @param email       user email
     */
    @Override
    public void sendSMSOTP(String phoneNumber, String email) {
        try {
            OTPBase otp = generateSMSOTP(phoneNumber, email);

            StreamPayload<StreamDataSMS> post = new StreamPayload<>();
            post.setEventType(StreamsEventType.SMS.toString());
            post.setInitiator(WAYAPAY);
            post.setToken(null);
            post.setKey(TWILIO_PROVIDER);

            StreamDataSMS data = new StreamDataSMS();
            data.setMessage(MESSAGE + otp.getCode() + MESSAGE_2);
            data.setRecipients(Collections.singletonList(new RecipientsSMS(email, "+".concat(otp.getPhoneNumber()))));

            post.setData(data);

            messageQueueProducer.send(SMS_TOPIC, post);
            log.info("otp sent to kafka message queue::: {}", post);

        } catch (Exception exception) {
            log.error("could not process data ", exception);
        }
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
            Optional<OTPBase> otpBase = otpRepository.getOtpDetails(phoneNumber, otp);

            if (otpBase.isPresent()) {
                var token = otpBase.get();
                LocalDateTime newTokenExpiryDate = token.getExpiryDate().minusHours(1);

                if (token.isValid()) {
                    otpRepository.updateToken(phoneNumber, token.getId(), newTokenExpiryDate, false);
                    //send a welcome email
                    CompletableFuture.runAsync(() -> sendWelcomeEmail(otpBase.get().getEmail()));

                    return new ApiResponse<OTPVerificationResponse>(new OTPVerificationResponse(true,
                            OTP_SUCCESS_MESSAGE), OTP_SUCCESS_MESSAGE, true);
                } else {
                    return new ApiResponse<OTPVerificationResponse>(new OTPVerificationResponse(false,
                            OTP_ERROR_MESSAGE), OTP_ERROR_MESSAGE, false);
                }
            }

            return new ApiResponse<>(new OTPVerificationResponse(false,
                    INVALID_OTP), INVALID_OTP, false);

        } catch (Exception exception) {
            log.error("could not process data ", exception);
            return new ApiResponse<>(new OTPVerificationResponse(false,
                    INVALID_OTP), INVALID_OTP, false);
        }
    }

    private void sendWelcomeEmail(String email) {

        var userProfile = profileRepository.findByEmail(false, email)
                .orElseThrow(() -> new CustomException("profile does not exist", HttpStatus.NOT_FOUND));

        StreamPayload<StreamDataEmail> post = new StreamPayload<>();
        post.setEventType(StreamsEventType.EMAIL.toString());
        post.setInitiator(WAYAPAY);
        post.setToken(null);
        post.setKey(TWILIO_PROVIDER);

        var data = new StreamDataEmail();
        data.setMessage(welcomeMessage.replace("xxxx", userProfile.getFirstName()));
        data.setNames(Collections.singletonList(new RecipientsEmail(email, userProfile.getFirstName())));

        post.setData(data);

        messageQueueProducer.send(EMAIL_TOPIC, post);
        log.info("sending welcome message kafka message queue::: {}", "post");
    }

}
