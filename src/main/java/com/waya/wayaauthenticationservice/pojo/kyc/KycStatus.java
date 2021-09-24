package com.waya.wayaauthenticationservice.pojo.kyc;

import lombok.Data;

@Data
public class KycStatus {
	
	private Long id;
	
	private Long userId;
	
	private KycTier tiers;

}
