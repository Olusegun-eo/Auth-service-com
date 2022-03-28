package com.waya.wayaauthenticationservice.pojo.mail.context;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;

public class AccountVerificationEmailContext extends AbstractEmailContext {

	@Override
	public <T> void init(T context) {
		// we can do any common configuration setup here
		// like setting up some base URL and context
		Users customer = (Users) context; // we pass the customer information
		put("firstName", customer.getFirstName());
		setDisplayName(customer.getFirstName());
		setEmail(customer.getEmail());
		setTemplateLocation("new-emails/email-verification-otp");
		setSubject("Complete your Email Verification");
		setTo(customer.getEmail());
		put("requestType", "Email Validation");
	}

	public void setToken(String token) {
		put("token", token);
	}

	public void buildURL(final String baseURL) {
//		final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
//								.path("/auth/email-verify/" + email)
//								.queryParam("token", token)
//								.toUriString();
		put("url", baseURL);
	}
}
