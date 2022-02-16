package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.TransactionResponsePojo;
import com.waya.wayaauthenticationservice.pojo.others.Transactions;
import com.waya.wayaauthenticationservice.response.ResponseObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "BILLER-SERVICE-API", configuration = AuthClientConfiguration.class)
public interface BillerProxy {

    @GetMapping("/api/v1/admin/get-transaction-count/{userId}")
    ResponseEntity<Long> getTransaction(@PathVariable String userId, @RequestHeader("Authorization") String token);

}
