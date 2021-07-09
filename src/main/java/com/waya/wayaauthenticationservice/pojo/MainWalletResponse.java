package com.waya.wayaauthenticationservice.pojo;

import lombok.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
@AllArgsConstructor
@JsonDeserialize
@Data
public class MainWalletResponse {

	private Long id;
	private String accountNo;
	private Integer clientId;
	private String clientName;
	private Long savingsProductId;
	private String savingsProductName;
	private Long fieldOfficerId;
	private Double nominalAnnualInterestRate;
	private WalletStatus status;
	private WalletTimeLine timeline;
	private WalletCurrency currency;
	private WalletSummary summary;
}
