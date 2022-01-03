package com.waya.wayaauthenticationservice.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.waya.wayaauthenticationservice.entity.PasswordPolicy;
import com.waya.wayaauthenticationservice.entity.UserSetup;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.kyc.KycAuthUpdate;
import com.waya.wayaauthenticationservice.pojo.kyc.KycStatus;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.proxy.KycProxy;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.PasswordPolicyRepository;
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
	
	@Autowired
	PasswordPolicyRepository passwordPolicyRepo;

	@Scheduled(cron = "${job.cron.5amED}")
	public void deleteExpiredPasswordToken() {
		LocalDateTime now = LocalDateTime.now();
		Integer count = otpRepository.deleteByExpiryDateLessThan(now);
		log.info("{} OTP Token(s) deleted", count);
	}

	@Scheduled(cron = "${job.cron.kyc}")
	public void updateKyc() {
		log.info("Update KYC");
		String key = "WAYA219766005KYC";
		ApiResponseBody<List<KycStatus>> listUser = kycProxy.GetUserKyc(key);
		List<KycStatus> listKyc = listUser.getData();
		if(listKyc != null && !listKyc.isEmpty()) {
			for(KycStatus mkyc : listKyc) {
				UserSetup user = userSetupRepository.GetByUserId(mkyc.getUserId());
				if(user == null) {
					log.info("USER SETUP LIMIT");
					UserSetupPojo pojo = new UserSetupPojo();
					pojo.setId(0L);
					pojo.setUserId(mkyc.getUserId());
					pojo.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
					userService.maintainUserSetup(pojo);
					user = userSetupRepository.GetByUserId(mkyc.getUserId());
				}
				if(user != null && !user.isUpdated()) {
					log.info("KYC UPDATE");
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
	}
	
	//@Scheduled(cron = "0 0/30 20-23 * * *")
	//@Scheduled(cron = "0/5 * * * * *")
	@Scheduled(cron = "${job.cron.pass}")
	private void updatePasswordToken() {
		List<Users> mUser = userRepository.findAll();
		for(Users user : mUser) {
			PasswordPolicy policy = passwordPolicyRepo.findByUserActive(user).orElse(null);
			if(policy != null && !user.isDeleted()) {
				int tokenAge = 0;
				int passwordAge = 0;
				LocalDate todate = LocalDate.now();
				LocalDate lastrun = policy.getRunDate();
				if(lastrun.compareTo(todate) < 0) {
					policy.setLchgDate(LocalDateTime.now());
					LocalDate passwordfrom = policy.getUpdatedPasswordDate();
					LocalDate tokenfrom = policy.getUpdatedTokenDate();
			        LocalDate to = LocalDate.now();
			        Period periodPas = Period.between(passwordfrom, to);
			        Period periodTok = Period.between(tokenfrom, to);
			        int totalPas = periodPas.getDays();
			        int totalTok = periodTok.getDays();
			        if(totalPas > 0 || totalTok > 0) {
			        	tokenAge = policy.getTokenAge() + totalTok;
			        	passwordAge = policy.getPasswordAge() + totalPas;
			        	policy.setPasswordAge(passwordAge);
			        	policy.setTokenAge(tokenAge);
			        }
			        policy.setRunDate(todate);
			        passwordPolicyRepo.save(policy);
				}
			}else if(policy != null && user.isDeleted()){
				policy.setDel_flg(true);
				policy.setLchgDate(LocalDateTime.now());
				policy.setRunDate(LocalDate.now());
				passwordPolicyRepo.save(policy);
			}else if(policy == null && !user.isDeleted()){
				PasswordPolicy mPolicy = new PasswordPolicy(user, user.getPassword());
				/*
				 * mPolicy.setDel_flg(false); mPolicy.setUser(user);
				 * mPolicy.setRcreDate(LocalDateTime.now());
				 * mPolicy.setLchgDate(LocalDateTime.now());
				 * mPolicy.setNewPassword(user.getPassword()); mPolicy.setPasswordCnt(0);
				 * mPolicy.setUpdatedPasswordDate(LocalDate.now());
				 * mPolicy.setChangePasswordDate(LocalDateTime.now());
				 * mPolicy.setPasswordAge(0); mPolicy.setUpdatedTokenDate(LocalDate.now());
				 * mPolicy.setChangeTokenDate(LocalDateTime.now());
				 * mPolicy.setRunDate(LocalDate.now()); mPolicy.setTokenAge(0);
				 */
				passwordPolicyRepo.save(mPolicy);
			}
		
		}
	}
	
	@Scheduled(cron = "${job.cron.pass}")
	public void kycAuthSink() {
		List<Users> mUser = userRepository.findAll();
		for(Users user : mUser) {
			if(user.isEmailVerified() && user.isPhoneVerified()) {
				String key = "WAYA219766005KYC";
				log.info("KYC POST");
				ApiResponseBody<KycStatus> userKyc = kycProxy.GetByUserKyc(key, user.getId());
				KycStatus kyc = userKyc.getData();
				if(kyc == null) {
					log.info("KYC-AUTH-POST");
					KycAuthUpdate mKyc = new KycAuthUpdate(user.getId(), false);
					kycProxy.PostKyc(key, mKyc);
				}
				
			}
		}
	}
}
