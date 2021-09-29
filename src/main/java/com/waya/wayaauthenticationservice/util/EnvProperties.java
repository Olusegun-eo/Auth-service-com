package com.waya.wayaauthenticationservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvProperties {
	
	@Autowired
	private Environment env;

	public String getCipherPass() { return env.getProperty("cipher.utils.key");}
	
	public String getTokenSecret() {
		return env.getProperty("jwt.secret");
	}
	
	public Long getTokenExpiryPeriod() {
		return Long.parseLong(env.getProperty("jwt.expiration"));
	}
}
