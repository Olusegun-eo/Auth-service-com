package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.RedisUser;
import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.repository.RedisUserDao;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.KafkaPushService;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/kafka")
@EnableCaching
public class KafkaPushController {


    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    KafkaPushService kafkaPushService;



    @ApiOperation(value = "Push Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events")
    @PostMapping("/create-profile-push")
    public ResponseEntity<?> createProfilePush(@RequestBody ProfilePojo profilePojo) {
        return authenticationService.createProfileAccount(profilePojo);
    }

    @ApiOperation(value = "Push Corporate Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a profile account after auth with Kafka events")
    @PostMapping("/create-corporate-profile-push")
    public ResponseEntity<?> createCorporateProfilePush(@RequestBody ProfilePojo2 profilePojo) {
        return authenticationService.createCorporateProfileAccount(profilePojo);
    }

    @ApiOperation(value = "Push Wallet Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a wallet account after auth with Kafka events")
    @PostMapping("/create-wallet-push")
    public ResponseEntity<?> createWalletPush(@RequestBody WalletPojo walletPojo) {
        return authenticationService.createWalletAccount(walletPojo);
    }

    @ApiOperation(value = "Push Third Party Account Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a Third Party Account after auth with Kafka events")
    @PostMapping("/create-virtual-account")
    public ResponseEntity<?> create3rdpartyPush(@RequestBody VirtualAccountPojo virtualAccountPojo) {
        return authenticationService.createVirtualAccount(virtualAccountPojo);
    }


    @ApiOperation(value = "Push Wayagram Profile Creation to Kafka (Service consumption only. Do not Use)", notes = "This endpoint is to create a Wayagram Account after auth with Kafka events")
    @PostMapping("/create-wayagram-account")
    public ResponseEntity<?> createWayagramPush(@RequestBody WayagramPojo wayagramPojo) {
        return authenticationService.createWayagramAccount(wayagramPojo);
    }

    @ApiOperation(value = "Push Wayagram Chats (Service consumption only. Do not Use)", notes = "This endpoint pushes chat to kafka for delayed persistence")
    @PostMapping("/post-chat")
    public ResponseEntity<?> postChat(@RequestBody ChatPojo chatPojo) {
        return kafkaPushService.postChat(chatPojo);
    }


}
