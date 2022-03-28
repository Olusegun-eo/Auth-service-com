package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.fraud.FraudEventRequestDTO;
import com.waya.wayaauthenticationservice.pojo.fraud.FraudEventResponseDTO;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "FRAUD-SERVICE-API", url = "${app.config.fraud-service.base-url}")
public interface FraudProxy {

    @PostMapping("/api/v1/fraud-event/report")
    @HandleFeignError(ApiClientExceptionHandler.class)
    ApiResponseBody<FraudEventResponseDTO> reportFraudEvent(@RequestBody FraudEventRequestDTO pojo);

}
