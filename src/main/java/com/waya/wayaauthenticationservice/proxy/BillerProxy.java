package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.Transactions;
import com.waya.wayaauthenticationservice.response.ResponseObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "BILLER-SERVICE-API", url = "${app.config.biller-service.base-url}", configuration = AuthClientConfiguration.class)
public interface BillerProxy {

    @GetMapping("/report/{username}")
    ResponseEntity<ResponseObj<List<Transactions>>> getTransaction(@PathVariable String username, @RequestHeader("Authorization") String token);

}
