package com.waya.wayaauthenticationservice.service.impl;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.streams.RecipientsEmail;
import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import com.waya.wayaauthenticationservice.streams.StreamPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateCode;

@Service
public class EmailServiceImpl implements EmailService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final OTPRepository otpRepository;
    private final MessageQueueProducer messageQueueProducer;

    private final Gson gson;

    @Autowired
    public EmailServiceImpl(OTPRepository otpRepository, MessageQueueProducer messageQueueProducer, Gson gson) {
        this.otpRepository = otpRepository;
        this.messageQueueProducer = messageQueueProducer;
        this.gson = gson;
    }

    /**
     * generates a 6 digit OTP code and send code to email topic
     * in kafka
     *
     * @param email    request
     * @param fullName name
     */
    @Override
    public boolean sendEmailToken(String email, String fullName) {
        try {
            //generate the token
            OTPBase otp = generateEmailToken(email);

            StreamPayload<StreamDataEmail> post = new StreamPayload<>();
            post.setEventType(StreamsEventType.EMAIL.toString());
            post.setInitiator(WAYAPAY);
            post.setToken(null);

            StreamDataEmail data = new StreamDataEmail();
            data.setMessage(VERIFY_EMAIL_TOKEN_MESSAGE + otp.getCode() + MESSAGE_2);
            data.setNames(Collections.singletonList(new RecipientsEmail(email, fullName)));

            post.setData(data);
            //send event to email topic in kafka
            CompletableFuture.runAsync(() -> messageQueueProducer.send(EMAIL_TOPIC, post));
            log.info("TOKEN sent to kafka message queue::: {}", post);
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
    private OTPBase generateEmailToken(String email) {
        OTPBase otp = new OTPBase();
        otp.setCode(generateCode());
        otp.setEmail(email);
        otp.setExpiryDate(120);

        //update previous otp expiry dates and isValid fields
        LocalDateTime newExpiryDate = LocalDateTime.now().minusHours(12);
        //invalidate the previous record
        otpRepository.invalidatePreviousRecordsViaEmail(email, newExpiryDate, false);
        otpRepository.save(otp);
        return otp;
    }

}
