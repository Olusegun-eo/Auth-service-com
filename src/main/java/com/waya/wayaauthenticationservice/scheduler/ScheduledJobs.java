package com.waya.wayaauthenticationservice.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.waya.wayaauthenticationservice.entity.UserSetup;
import com.waya.wayaauthenticationservice.pojo.kyc.KycAuthUpdate;
import com.waya.wayaauthenticationservice.pojo.kyc.KycStatus;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.proxy.KycProxy;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.repository.UserSetupRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

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
	
	@Autowired
	UserSetupRepository userSetupRepository;
	
	@Autowired
	KycProxy kycProxy;

	@Scheduled(cron = "${job.cron.5amED}")
	public void deleteExpiredPasswordToken() {
		LocalDateTime now = LocalDateTime.now();
		Integer count = otpRepository.deleteByExpiryDateLessThan(now);
		log.info("{} OTP Token(s) deleted", count);
	}

	/*@Scheduled(cron = "${job.cron.kyc}")
	public void updateKyc() {
		log.info("Update KYC");
		String key = "WAYA219766005KYC";
		ApiResponseBody<List<KycStatus>> listUser = kycProxy.GetUserKyc(key);
		List<KycStatus> listKyc = listUser.getData();
		if(listKyc != null && !listKyc.isEmpty()) {
			for(KycStatus mkyc : listKyc) {
				UserSetup user = userSetupRepository.GetByUserId(mkyc.getUserId());
				if(user == null) {
					UserSetupPojo pojo = new UserSetupPojo();
					pojo.setId(0L);
					pojo.setUserId(mkyc.getUserId());
					pojo.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
					userService.maintainUserSetup(pojo);
					user = userSetupRepository.GetByUserId(mkyc.getUserId());
				}
				if(user != null && !user.isUpdated()) {
					user.setUpdated(true);
					userSetupRepository.save(user);
					KycAuthUpdate uKyc = new KycAuthUpdate();
					uKyc.setUserId(mkyc.getUserId());
					uKyc.setKcyupdate(true);
					kycProxy.PostKycUpdate(key, uKyc);
				}
				
				if(user != null && user.isUpdated() && !mkyc.isProcessFlg()) {
					user.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
					userSetupRepository.save(user);
				}
				
			}
		}
	}*/

}
