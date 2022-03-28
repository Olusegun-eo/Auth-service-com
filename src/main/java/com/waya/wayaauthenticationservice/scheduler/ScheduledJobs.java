package com.waya.wayaauthenticationservice.scheduler;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.waya.wayaauthenticationservice.entity.PasswordPolicy;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.UserSetup;
import com.waya.wayaauthenticationservice.entity.UserWallet;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.MyWallet;
import com.waya.wayaauthenticationservice.pojo.SettleUserRequest;
import com.waya.wayaauthenticationservice.pojo.WalletResponse;
import com.waya.wayaauthenticationservice.pojo.kyc.KycAuthUpdate;
import com.waya.wayaauthenticationservice.pojo.kyc.KycStatus;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.proxy.IdentityManagerProxy;
import com.waya.wayaauthenticationservice.proxy.KycProxy;
import com.waya.wayaauthenticationservice.proxy.SettlementProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.repository.PasswordPolicyRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.repository.UserSetupRepository;
import com.waya.wayaauthenticationservice.repository.UserWalletRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.IdentityData;
import com.waya.wayaauthenticationservice.response.IdentityResponse;
import com.waya.wayaauthenticationservice.response.SettleUserResponse;
import com.waya.wayaauthenticationservice.service.UserService;

import feign.FeignException;
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
	WalletProxy walletProxy;

	@Autowired
	IdentityManagerProxy identManagerProxy;

	@Autowired
	SettlementProxy settleProxy;

	@Autowired
	PasswordPolicyRepository passwordPolicyRepo;

	@Autowired
	UserWalletRepository userWalletRepo;

	@Autowired
	ProfileRepository profileRepo;

	@Scheduled(cron = "${job.cron.5amED}")
	public void deleteExpiredPasswordToken() {
		LocalDateTime now = LocalDateTime.now();
		Integer count = otpRepository.deleteByExpiryDateLessThan(now);
		log.info("{} OTP Token(s) deleted", count);
	}

	// @Scheduled(cron = "${job.cron.kyc}")
	@Scheduled(cron = "${job.cron.pass}")
	public void updateKyc() {
		// log.info("Update KYC");
		String key = "WAYA219766005KYC";
		ApiResponseBody<List<KycStatus>> listUser = new ApiResponseBody<List<KycStatus>>();
		try {
			listUser = kycProxy.GetUserKyc(key);
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			log.error("KYC UPDATE PROXY: " + ex.getLocalizedMessage());
		}
		// log.info("KYC SUCCESS: " +listUser.getStatus());
		// log.info("DATA KYC: " + listUser.getData());
		if (listUser.getStatus()) {
			List<KycStatus> listKyc = listUser.getData();

			if (listKyc != null && !listKyc.isEmpty()) {
				log.info("KYC SINKING");
				for (KycStatus mkyc : listKyc) {
					UserSetup user = userSetupRepository.GetByUserId(mkyc.getUserId());
					Users mUser = userRepository.findById(mkyc.getUserId()).orElse(null);
					if (mUser != null) {
						if (user == null && !mUser.isDeleted()) {
							log.info("USER SETUP LIMIT");
							UserSetupPojo pojo = new UserSetupPojo();
							pojo.setId(0L);
							pojo.setUserId(mkyc.getUserId());
							pojo.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
							userService.maintainUserSetup(pojo);
							user = userSetupRepository.GetByUserId(mkyc.getUserId());
						}
						if (user != null && !user.isUpdated()) {
							log.info("KYC UPDATE");
							try {
								user.setUpdated(true);
								userSetupRepository.save(user);
								KycAuthUpdate uKyc = new KycAuthUpdate();
								uKyc.setUserId(mkyc.getUserId());
								uKyc.setKcyupdate(true);
								kycProxy.PostKycUpdate(key, uKyc);
							} catch (Exception ex) {
								if (ex instanceof FeignException) {
									String httpStatus = Integer.toString(((FeignException) ex).status());
									log.error("Feign Exception Status {}", httpStatus);
								}
								log.error("Higher Wahala {}", ex.getMessage());
								log.error("KYC POST UPDATE PROXY: " + ex.getLocalizedMessage());
							}
						}

						if (user != null && user.isUpdated() && !mkyc.isProcessFlg()) {
							user.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
							userSetupRepository.save(user);
						}

						if (user != null && user.isUpdated() && mkyc.isProcessFlg()) {
							log.info("KYC LIMIT INCREASE");
							int res = user.getTransactionLimit().compareTo(mkyc.getTiers().getMaximumLimit());
							if (res != 0) {
								user.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
								userSetupRepository.save(user);
							}
						}

					}
				}
			}
		}
	}

	// @Scheduled(cron = "${job.cron.kyc}")
	@Scheduled(cron = "${job.cron.pass}")
	public void postKycUpdate() {
		// log.info("Update KYC");
		String key = "WAYA219766005KYC";
		ApiResponseBody<List<KycStatus>> listUser = new ApiResponseBody<List<KycStatus>>();
		try {
			listUser = kycProxy.GetChangeKyc(key);
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			log.error("KYC UPDATE PROXY: " + ex.getLocalizedMessage());
		}
		// log.info("KYC SUCCESS: " +listUser.getStatus());
		// log.info("DATA KYC: " + listUser.getData());
		if (listUser.getStatus()) {
			List<KycStatus> listKyc = listUser.getData();
			if (listKyc != null && !listKyc.isEmpty()) {
				// log.info("KYC SINKING");
				for (KycStatus mkyc : listKyc) {
					UserSetup user = userSetupRepository.GetByUserId(mkyc.getUserId());
					Users mUser = userRepository.findById(mkyc.getUserId()).orElse(null);
					if (mUser != null) {
						if (user == null && !mUser.isDeleted()) {
							// log.info("USER SETUP LIMIT");
							UserSetupPojo pojo = new UserSetupPojo();
							pojo.setId(0L);
							pojo.setUserId(mkyc.getUserId());
							pojo.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
							userService.maintainUserSetup(pojo);
							user = userSetupRepository.GetByUserId(mkyc.getUserId());
						}
						if (user != null && !user.isUpdated()) {
							log.info("KYC UPDATE");
							try {
								user.setUpdated(true);
								userSetupRepository.save(user);
								KycAuthUpdate uKyc = new KycAuthUpdate();
								uKyc.setUserId(mkyc.getUserId());
								uKyc.setKcyupdate(true);
								kycProxy.PostKycUpdate(key, uKyc);
							} catch (Exception ex) {
								if (ex instanceof FeignException) {
									String httpStatus = Integer.toString(((FeignException) ex).status());
									log.error("Feign Exception Status {}", httpStatus);
								}
								log.error("Higher Wahala {}", ex.getMessage());
								log.error("KYC UPDATE PROXY: " + ex.getLocalizedMessage());
							}
						}

						if (user != null && user.isUpdated() && !mkyc.isProcessFlg()) {
							user.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
							userSetupRepository.save(user);
						}

						if (user != null && user.isUpdated() && mkyc.isProcessFlg()) {
							log.info("KYC LIMIT INCREASE");
							int res = user.getTransactionLimit().compareTo(mkyc.getTiers().getMaximumLimit());
							if (res != 0) {
								user.setTransactionLimit(mkyc.getTiers().getMaximumLimit());
								userSetupRepository.save(user);
							}
						}

					}
				}
			}
		}
	}

	// @Scheduled(cron = "0 0/30 20-23 * * *")
	// @Scheduled(cron = "0/5 * * * * *")
	@Scheduled(cron = "${job.cron.pass}")
	private void updatePasswordToken() {
		List<Users> mUser = userRepository.findAll();
		for (Users user : mUser) {
			PasswordPolicy policy = passwordPolicyRepo.findByUserActive(user).orElse(null);
			if (policy != null && !user.isDeleted()) {
				int tokenAge = 0;
				int passwordAge = 0;
				LocalDate todate = LocalDate.now();
				LocalDate lastrun = policy.getRunDate();
				if (lastrun.compareTo(todate) < 0) {
					policy.setLchgDate(LocalDateTime.now());
					LocalDate passwordfrom = policy.getUpdatedPasswordDate();
					LocalDate tokenfrom = policy.getUpdatedTokenDate();
					LocalDate to = LocalDate.now();
					Period periodPas = Period.between(passwordfrom, to);
					Period periodTok = Period.between(tokenfrom, to);
					int totalPas = periodPas.getDays();
					int totalTok = periodTok.getDays();
					if (totalPas > 0 || totalTok > 0) {
						tokenAge = policy.getTokenAge() + totalTok;
						passwordAge = policy.getPasswordAge() + totalPas;
						policy.setPasswordAge(passwordAge);
						policy.setTokenAge(tokenAge);
					}
					policy.setRunDate(todate);
					passwordPolicyRepo.save(policy);
				}
			} else if (policy != null && user.isDeleted()) {
				policy.setDel_flg(true);
				policy.setLchgDate(LocalDateTime.now());
				policy.setRunDate(LocalDate.now());
				passwordPolicyRepo.save(policy);
			} else if (policy == null && !user.isDeleted()) {
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
		for (Users user : mUser) {
			if (user.isEmailVerified() && user.isPhoneVerified() && !user.isDeleted()) {
				String key = "WAYA219766005KYC";
				// log.info("KYC POST");
				KycStatus kyc = new KycStatus();
				try {
					ApiResponseBody<KycStatus> userKyc = kycProxy.GetByUserKyc(key, user.getId());
					kyc = userKyc.getData();
				} catch (Exception ex) {
					if (ex instanceof FeignException) {
						String httpStatus = Integer.toString(((FeignException) ex).status());
						log.error("Feign Exception Status {}", httpStatus);
					}
					log.error("Higher Wahala {}", ex.getMessage());
					log.error("KYC UPDATE PROXY: " + ex.getLocalizedMessage());
				}
				if (kyc == null) {
					log.info("KYC-AUTH-POST");
					KycAuthUpdate mKyc = new KycAuthUpdate(user.getId(), false);
					kycProxy.PostKyc(key, mKyc);
				}

			}
		}
	}

	@Scheduled(cron = "${job.cron.pass}")
	public void MerchantAuthSink() {
		List<Users> mUser = userRepository.findAll();
		for (Users user : mUser) {
			String merchantId = user.getMerchantId();
			if (user.isActive() && user.isCorporate() && !user.isDeleted()) {
				if (merchantId == null) {
					IdentityData ident = new IdentityData();
					try {
						IdentityResponse userIdent = identManagerProxy.PostCreateMerchant(user.getId());
						ident = userIdent.getData();
					} catch (Exception ex) {
						if (ex instanceof FeignException) {
							String httpStatus = Integer.toString(((FeignException) ex).status());
							log.error("Feign Exception Status {}", httpStatus);
						}
						log.error("Higher Wahala {}", ex.getMessage());
						log.error("KYC UPDATE PROXY: " + ex.getLocalizedMessage());
					}
					if (ident != null) {
						log.info("IDENTITY-AUTH-POST: " + ident.getMerchantId() + " WITH USER ID: " + user.getId());
						Users kUser = userRepository.findById(user.getId()).orElse(null);
						if (kUser != null) {
							kUser.setMerchantId(ident.getMerchantId());
							userRepository.save(kUser);
						}
					}
				}

			}
		}
	}

	@Scheduled(cron = "${job.cron.pass}")
	public void SettlementAuthSink() {
		List<Users> mUser = userRepository.findAll();
		for (Users user : mUser) {
			if (!user.isDeleted() && !user.isSimulated()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				String createAt = sdf.format(new Date());
				SettleUserResponse response = new SettleUserResponse();
				response.setStatus(false);
				response.setMessage("INVALID RESPONSE");
				response.setData(null);
				response.setTimeStamp(createAt);
				try {
					response = settleProxy.GetSettleUser(user.getId(), "WAYA0984SETTLE2022");
				} catch (Exception ex) {
					if (ex instanceof FeignException) {
						String httpStatus = Integer.toString(((FeignException) ex).status());
						log.error("Feign Exception Status {}", httpStatus);
					}
					log.error("Higher Wahala {}", ex.getMessage());
					log.error("SETTLE PROXY: " + ex.getLocalizedMessage());
				}
				if (!response.isStatus()) {
					String status = null;
					String usertype = null;
					if (user.isCorporate()) {
						usertype = "C";
					} else {
						usertype = "I";
					}
					if (user.isActive()) {
						status = "ACTIVE";
					} else {
						status = "INACTIVE";
					}
					SettleUserRequest request = new SettleUserRequest(createAt, user.getEmail(), user.getName(),
							user.getPhoneNumber(), status, user.getId(), usertype);
					SettleUserResponse userResp = new SettleUserResponse();
					userResp.setStatus(false);
					userResp.setMessage("INVALID RESPONSE");
					userResp.setData(null);
					userResp.setTimeStamp(createAt);
					try {
						userResp = settleProxy.PostSettleUser(request, "WAYA0984SETTLE2022");
					} catch (Exception ex) {
						if (ex instanceof FeignException) {
							String httpStatus = Integer.toString(((FeignException) ex).status());
							log.error("Feign Exception Status {}", httpStatus);
						}
						log.error("Higher Wahala {}", ex.getMessage());
						log.error("SETTLE CREATION PROXY: " + ex.getLocalizedMessage());
					}
					if (userResp.isStatus()) {
						log.info("SETTLEMENT-POST WITH USER ID: " + userResp.getData().getUserId());
					}
				}

			}
		}
	}

	@Scheduled(cron = "${job.cron.pass}")
	public void AuthWalletSink() {
		List<Users> mUser = userRepository.findAll();
		for (Users user : mUser) {
			UserWallet sUser = userWalletRepo.findByUserId(user.getId()).orElse(null);
			if (sUser == null) {
				String status = "INACTIVE";
				String usertype = "I";
				if (user.isActive()) {
					status = "ACTIVE";
				}
				if (!user.isAdmin() && user.isCorporate()) {
					usertype = "C";
				}
				UserWallet kyc = null;
				Profile mProfile = profileRepo.findByUserId(user.getId().toString()).orElse(null);
				if (mProfile != null) {
					kyc = new UserWallet(user.isDeleted(), user.getId(), user.getName(), user.getPhoneNumber(),
							user.getEmail(), mProfile.getCity(), mProfile.getDistrict(), false, status, usertype,
							user.getCreatedAt(), "");
				} else {
					kyc = new UserWallet(user.isDeleted(), user.getId(), user.getName(), user.getPhoneNumber(),
							user.getEmail(), "", "", false, status, usertype, user.getCreatedAt(), "");
				}
				userWalletRepo.save(kyc);
			} else {
				Long userId = sUser.getId();
				// isCardLinked, wallet, isWebPos and isterminalPos
				if (!sUser.isWebPos() && !user.isDeleted()) {

				}
				if (!sUser.isTerminalPos() && !user.isDeleted()) {

				}
				if (!sUser.isCardLinked() && !user.isDeleted()) {

				}
				if (sUser.getWallet().isBlank() && !user.isDeleted() && !sUser.isDeleted()) {
					try {
						WalletResponse wallet = walletProxy.getTotalWallet(userId);
						if (wallet != null && wallet.isStatus()) {
							List<MyWallet> totwallet = wallet.getData();
							String walletsize = Integer.toString(totwallet.size());
							sUser.setWallet(walletsize);
							userWalletRepo.save(sUser);
						}
					} catch (FeignException ex) {
						// log.error(ex.getMessage());
					}
				}
			}
		}
	}
}
