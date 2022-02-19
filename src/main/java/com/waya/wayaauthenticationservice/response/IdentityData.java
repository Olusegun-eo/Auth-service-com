package com.waya.wayaauthenticationservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IdentityData {
	
	private String merchantId;
	private String refNo;
	private String merchantPublicTestKey;
	private String merchantKeyMode;

}
