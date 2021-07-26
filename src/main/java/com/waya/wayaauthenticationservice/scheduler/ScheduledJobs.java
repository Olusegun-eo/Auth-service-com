package com.waya.wayaauthenticationservice.scheduler;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@Slf4j
public class ScheduledJobs {

    @Autowired
    OTPRepository otpRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Scheduled(cron = "${job.cron.5amED}")
    public void deleteExpiredPasswordToken() {
        LocalDateTime now = LocalDateTime.now();
        Integer count = otpRepository.deleteByExpiryDateLessThan(now);
        log.info("{} OTP Token(s) deleted", count);
    }

    //@Scheduled(cron = "${job.cron.5min1318}")
    //@Scheduled(cron = "*/30 * * * * *")
    public void deleteAllWallets() {
        int count = 0;
        String token = "serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1ODU2ODA1NH0.N7laM6vqTNwmYo-EPpoiAIx_sGUes_ruYLF5ynt45y4";

        List<Users> users = userRepository.findAll()
                .stream().filter(user -> user.isDeleted())
                .collect(Collectors.toList());
        for(Users user : users){
           userService.deactivationServices(user, token);
           ++count;
        }
        log.info("{} External Service Deleted deleted", count);
    }

}
