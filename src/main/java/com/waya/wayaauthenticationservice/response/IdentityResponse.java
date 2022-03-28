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
public class IdentityResponse {
	
	private String code;
	
	private String message;
	
	private IdentityData data;

}
