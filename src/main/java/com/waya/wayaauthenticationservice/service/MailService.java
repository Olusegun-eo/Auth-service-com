package com.waya.wayaauthenticationservice.service;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;
import com.waya.wayaauthenticationservice.streams.RecipientsEmail;
import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import com.waya.wayaauthenticationservice.streams.StreamPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Collections;

import static com.waya.wayaauthenticationservice.util.Constant.*;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private final SpringTemplateEngine templateEngine;
    private final MessageQueueProducer messageQueueProducer;

    public void sendMail(AbstractEmailContext email) throws MessagingException {

        Context context = new Context();
        context.setVariables(email.getContext());

        StreamPayload<StreamDataEmail> post = new StreamPayload<>();
        post.setEventType(StreamsEventType.EMAIL.toString());
        post.setInitiator(WAYAPAY);
        post.setToken(null);
        post.setKey(TWILIO_PROVIDER);

        StreamDataEmail data = new StreamDataEmail();

        String emailContent = templateEngine.process(email.getTemplateLocation(), context);
        data.setMessage(emailContent);
        data.setNames(Collections.singletonList(new RecipientsEmail(email.getEmail(), email.getDisplayName())));
        post.setData(data);

        messageQueueProducer.send(EMAIL_TOPIC, post);
        log.info("sending Email message kafka message queue::: {}", new Gson().toJson(post));
    }
}
