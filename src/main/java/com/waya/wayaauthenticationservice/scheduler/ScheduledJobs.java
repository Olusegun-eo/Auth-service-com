package com.waya.wayaauthenticationservice.scheduler;

import com.waya.wayaauthenticationservice.repository.OTPRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@Slf4j
public class ScheduledJobs {

    @Autowired
    OTPRepository otpRepository;

    @Scheduled(cron = "${job.cron.5amED}")
    public void deleteExpiredPasswordToken() {
        LocalDateTime now = LocalDateTime.now();
        Integer count = otpRepository.deleteByExpiryDateLessThan(now);
        log.info("{} OTP Token(s) deleted", count);
    }

}
