package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.mail.MailTokenRequest;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import com.waya.wayaauthenticationservice.service.EmailService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waya.wayaauthenticationservice.response.ApiResponse;

import javax.validation.Valid;

import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

@Api(tags = {"Email Verification Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "Email Verification Resource", description = "REST API for Email Verification.")
})
@CrossOrigin
@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @ApiOperation(
            value = "${api.email.email-token.description}",
            notes = "${api.email.email-token.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("email-token/{email}/{userName}")
    public ResponseEntity<ApiResponse<Object>> getEmailToken(@Valid @RequestBody MailTokenRequest mailToken) {
        emailService.sendEmailToken(mailToken.getEmail(), mailToken.getName(), mailToken.getMessage());
        return new ResponseEntity<>(new ApiResponse<>(null, "A token has been sent to your email",
                true), HttpStatus.OK);
    }

    /**
     * endpoint to verify an email
     *
     * @param email email
     * @param token token
     * @return Object
     * @author Chisom.Iwowo
     */
    @ApiOperation(
            value = "${api.email.email-verify.description}",
            notes = "${api.email.email-verify.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("email-verify/{email}/{token}")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(@PathVariable String email,
                                                                              @PathVariable Integer token) {
        EmailVerificationResponse verificationResponse = emailService.verifyEmailToken(email, token);
        ApiResponse<EmailVerificationResponse> response = new ApiResponse<>(verificationResponse,
                verificationResponse.getMessage(), verificationResponse.isValid());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
