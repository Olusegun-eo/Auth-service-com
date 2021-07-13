package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.others.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.KafkaPushService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/kafka")
@Tag(name = "KAFKA", description = "Kafka Service API")
@EnableCaching
public class KafkaPushController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    KafkaPushService kafkaPushService;

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @ApiOperation(value = "Push Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events", tags = { "KAFKA" })
    @PostMapping("/create-profile-push")
    public ResponseEntity<?> createProfilePush(@RequestBody PersonalProfileRequest profilePojo, final HttpServletRequest request) {
        return authenticationService.createProfileAccount(profilePojo, getBaseUrl(request));
    }

    @ApiOperation(value = "Push Corporate Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-corporate-profile-push")
    public ResponseEntity<?> createCorporateProfilePush(@RequestBody CorporateProfileRequest profilePojo, final HttpServletRequest request) {
        return authenticationService.createCorporateProfileAccount(profilePojo, getBaseUrl(request));
    }

    @ApiOperation(value = "Push Wallet Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a wallet account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-wallet-push")
    public ResponseEntity<?> createWalletPush(@RequestBody WalletPojo walletPojo) {
        return authenticationService.createWalletAccount(walletPojo);
    }

    @ApiOperation(value = "Push Third Party Account Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a Third Party Account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-virtual-account")
    public ResponseEntity<?> create3rdPartyPush(@RequestBody VirtualAccountPojo virtualAccountPojo) {
        return authenticationService.createVirtualAccount(virtualAccountPojo);
    }

    @ApiOperation(value = "Push Wayagram Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a Wayagram Account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-wayagram-account")
    public ResponseEntity<?> createWayagramPush(@RequestBody WayagramPojo wayagramPojo) {
        return authenticationService.createWayagramAccount(wayagramPojo);
    }

    @ApiOperation(value = "Push Wayagram Chats (Service consumption only. Do not Use)", notes = "This endpoint pushes chat to kafka for delayed persistence" , tags = { "KAFKA" })
    @PostMapping("/post-chat")
    public ResponseEntity<?> postChat(@RequestBody ChatPojo chatPojo) {
        return kafkaPushService.postChat(chatPojo);
    }


}
