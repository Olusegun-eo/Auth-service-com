package com.waya.wayaauthenticationservice.pojo.others;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletSummary {

	private WalletCurrency currency;
	private Double accountBalance;
    private Double availableBalance;
}
