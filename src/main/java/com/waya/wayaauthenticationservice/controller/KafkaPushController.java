package com.waya.wayaauthenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.pojo.ChatPojo;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo2;
import com.waya.wayaauthenticationservice.pojo.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.WalletPojo;
import com.waya.wayaauthenticationservice.pojo.WayagramPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.KafkaPushService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;

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



    @ApiOperation(value = "Push Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events", tags = { "KAFKA" })
    @PostMapping("/create-profile-push")
    public ResponseEntity<?> createProfilePush(@RequestBody ProfilePojo profilePojo) {
        return authenticationService.createProfileAccount(profilePojo);
    }

    @ApiOperation(value = "Push Corporate Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-corporate-profile-push")
    public ResponseEntity<?> createCorporateProfilePush(@RequestBody ProfilePojo2 profilePojo) {
        return authenticationService.createCorporateProfileAccount(profilePojo);
    }

    @ApiOperation(value = "Push Wallet Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a wallet account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-wallet-push")
    public ResponseEntity<?> createWalletPush(@RequestBody WalletPojo walletPojo) {
        return authenticationService.createWalletAccount(walletPojo);
    }

    @ApiOperation(value = "Push Third Party Account Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a Third Party Account after auth with Kafka events" , tags = { "KAFKA" })
    @PostMapping("/create-virtual-account")
    public ResponseEntity<?> create3rdpartyPush(@RequestBody VirtualAccountPojo virtualAccountPojo) {
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
