package com.waya.wayaauthenticationservice.pojo.mail.context;

import org.springframework.web.util.UriComponentsBuilder;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;

public class AccountVerificationEmailContext extends AbstractEmailContext {

	@Override
	public <T> void init(T context) {
		// we can do any common configuration setup here
		// like setting up some base URL and context
		Profile customer = (Profile) context; // we pass the customer information
		put("firstName", customer.getFirstName());
		setDisplayName(customer.getFirstName());
		setEmail(customer.getEmail());
		setTemplateLocation("emails/email-verification");
		setSubject("Complete your Email Verification");
		setFrom("iyabobostores@gmail.com");
		setTo(customer.getEmail());
		put("requestType", "Email Validation");
	}

	public void setToken(String token) {
		put("token", token);
	}

	public void buildURL(final String baseURL, String email, final String token) {
		final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
								.path("email-verify/" + email)
								.queryParam("token", token)
								.toUriString();
		put("url", url);
	}
}
