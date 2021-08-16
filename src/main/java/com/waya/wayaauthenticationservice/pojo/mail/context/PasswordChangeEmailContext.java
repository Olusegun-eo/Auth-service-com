package com.waya.wayaauthenticationservice.pojo.mail.context;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;

public class PasswordChangeEmailContext extends AbstractEmailContext {

	@Override
	public <T> void init(T context) {
		// we can do any common configuration setup here
		// like setting up some base URL and context
		Users customer = (Users) context; // we pass the customer information
		put("firstName", customer.getFirstName());
		put("requestType", "Password Change");
		setTemplateLocation("emails/password-change");
		setSubject("You Just Changed Your Password!!!");
		setTo(customer.getEmail());
		setDisplayName(customer.getFirstName());
		setEmail(customer.getEmail());
	}

	public void setToken(String token) {
		put("token", token);
	}

	public void redirectTo(final String baseURL){
		put("url", baseURL);
	}

}
