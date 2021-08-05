package com.waya.wayaauthenticationservice.pojo.mail.context;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;

public class AdminCheckContext extends AbstractEmailContext {

    @Override
    public <T> void init(T context) {
        // we can do any common configuration setup here
        // like setting up some base URL and context
        Profile customer = (Profile) context; // we pass the customer information
        put("firstName", customer.getFirstName());
        put("requestType", "Admin Action");
        setTemplateLocation("emails/admin-check");
        setSubject("Confirm Admin Action !!!");
        setEmail(customer.getEmail());
        setDisplayName(customer.getFirstName());
        setTo(customer.getEmail());
    }

    public void seToken(final String token) {
        put("token", token);
    }

}
