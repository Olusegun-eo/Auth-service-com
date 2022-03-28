package com.waya.wayaauthenticationservice.pojo.kyc;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KycStatus {
	
	private Long id;
	
	private Long userId;
	
	private KycTier tiers;
	
	private boolean processFlg;

}
