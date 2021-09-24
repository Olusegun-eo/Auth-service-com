package com.waya.wayaauthenticationservice.pojo.kyc;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class KycTier {

	private Long id;

	private BigDecimal maximumLimit;

	private BigDecimal singleTransferLimit;

	private int maxNumberOfTxnAllowed;

	private String tiers;

}
