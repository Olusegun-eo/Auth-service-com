package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.fraud.FraudIDPojo;
import com.waya.wayaauthenticationservice.service.FraudService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/fraud-actions")
@Tag(name = "FRAUDACTIONS", description = "Fraud Action Services for Authentication Service API")
@AllArgsConstructor
@Validated
public class FraudActionsController {

    private final FraudService fraudService;

    @ApiOperation(value = "Get User Details User by ID (In-app use only)", tags = {"FRAUDACTIONS"})
    @GetMapping("/user-details/{id}")
    public ResponseEntity<?> findUser(@PathVariable @Min(value = 1L, message = "Id Passed must be positive") Long id,
                                      @RequestHeader("ApiKey") String apiKey) {
        return fraudService.findUserById(id, apiKey);
    }

    @ApiOperation(value = "Toggle Lock on User's Account by ID (In-app use only)", tags = {"FRAUDACTIONS"})
    @PostMapping("/user/toggle-lock")
    public ResponseEntity<?> lockUser(@Valid @RequestBody FraudIDPojo pojo) {
        return fraudService.toggleUserLock(pojo);
    }

    @ApiOperation(value = "Toggle Activation on User's Account by ID (In-app use only)", tags = {"FRAUDACTIONS"})
    @PostMapping("/user/toggle-activation")
    public ResponseEntity<?> activateUser(@Valid @RequestBody FraudIDPojo pojo) {
        return fraudService.toggleUserActivation(pojo);
    }

    @ApiOperation(value = "Close User's Account by ID (In-app use only)", tags = {"FRAUDACTIONS"})
    @PostMapping("/user/close-account")
    public ResponseEntity<?> closeAccount(@Valid @RequestBody FraudIDPojo pojo) {
        return fraudService.closeAccount(pojo);
    }

}
