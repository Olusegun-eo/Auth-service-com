package com.waya.wayaauthenticationservice.pojo.mail.context;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;

public class PinResetContext extends AbstractEmailContext {

    @Override
    public <T> void init(T context) {
        // we can do any common configuration setup here
        // like setting up some base URL and context
        Users customer = (Users) context; // we pass the customer information
        put("firstName", customer.getFirstName());
        put("requestType", "Pin Reset");
       // setTemplateLocation("emails/pin-reset");
        setTemplateLocation("new-emails/pin-reset-notification");
        setSubject("Reset Pin !!!");
        setEmail(customer.getEmail());
        setDisplayName(customer.getFirstName());
        setTo(customer.getEmail());
    }

    public void seToken(final String token){
        put("token", token);
    }

    public void redirectTo(final String passwordResetURL) {
        put("url", passwordResetURL);
    }

}
