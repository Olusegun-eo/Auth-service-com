package com.waya.wayaauthenticationservice.pojo.mail.context;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;
import org.springframework.web.util.UriComponentsBuilder;

public class WelcomeEmailContext extends AbstractEmailContext {

    @Override
    public <T> void init(T context) {
        // we can do any common configuration setup here
        // like setting up some base URL and context
        Profile customer = (Profile) context; // we pass the customer information
        put("firstName", customer.getFirstName());
        setDisplayName(customer.getFirstName());
        setEmail(customer.getEmail());
        setTemplateLocation("emails/welcome-mail");
        setSubject("Welcome to the Fold \uD83D\uDC83 \uD83D\uDC83");
        setFrom("WAYA PayChat");
    }


}
