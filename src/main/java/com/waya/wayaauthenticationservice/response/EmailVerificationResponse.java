package com.waya.wayaauthenticationservice.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailVerificationResponse implements Serializable {

	private static final long serialVersionUID = -3053054370548259496L;
	private boolean valid;
    private String message;
}
