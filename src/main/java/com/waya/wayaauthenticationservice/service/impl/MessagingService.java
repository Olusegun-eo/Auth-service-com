package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.enums.EventCategory;
import com.waya.wayaauthenticationservice.enums.StreamsEventType;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;
import com.waya.wayaauthenticationservice.pojo.notification.NotificationResponsePojo;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import com.waya.wayaauthenticationservice.streams.*;
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
public class MessagingService {

    private final SpringTemplateEngine templateEngine;
    private final MessageQueueProducer messageQueueProducer;
    private final NotificationProxy proxy;

    public void sendMail(AbstractEmailContext email) throws MessagingException {
        try {
            /* update made by Terseer 29/12/2021 */
            NotificationResponsePojo notificationResponsePojo = new NotificationResponsePojo();
            //notificationResponsePojo.setEventCategory(EventCategory.WELCOME.name());
            notificationResponsePojo.setEventType(StreamsEventType.EMAIL.toString());
            notificationResponsePojo.setInitiator(WAYAPAY);



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

            /* update made by Terseer 29/12/2021 */
            notificationResponsePojo.setData(data);
            notificationResponsePojo.setProductType("WAYABANK");

    //      messageQueueProducer.send(EMAIL_TOPIC, post);

           proxy.sendEmail(notificationResponsePojo);
        // send to notification service
        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
        }
        //log.info("sending Email message kafka message queue::: {}", new Gson().toJson(post));
    }

    public void sendSMS(String name, String message, String phoneNumber) {
        try {
            StreamPayload<StreamDataSMS> post = new StreamPayload<>();
            post.setEventType(StreamsEventType.SMS.toString());
            post.setInitiator(WAYAPAY);
            post.setToken(null);
            post.setKey(TWILIO_PROVIDER);

            StreamDataSMS data = new StreamDataSMS();
            data.setMessage(message);
            data.setRecipients(Collections.singletonList(new RecipientsSMS(name, "+".concat(phoneNumber))));

            post.setData(data);
            messageQueueProducer.send(SMS_TOPIC, post);
        } catch (Exception exception) {
            log.error("could not process data {}", exception.getMessage());
        }
    }
}
