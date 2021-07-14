package com.waya.wayaauthenticationservice.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OTPVerificationResponse implements Serializable {

	private static final long serialVersionUID = 2535303439894069556L;
	private boolean valid;
    private String message;
}

