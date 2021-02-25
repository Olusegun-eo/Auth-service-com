package com.waya.wayaauthenticationservice.service.impl;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class KafkaMessageProducer implements MessageQueueProducer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> template;
    private final Gson gson;

    @Autowired
    public KafkaMessageProducer(KafkaTemplate<String, Object> template, Gson gson) {
        this.template = template;
        this.gson = gson;
    }


    /**
     * Non Blocking (Async), sends data to kafka
     * @param topic
     * @param data
     */
    @Override
    public void send(String topic, Object data) {
        ListenableFuture<SendResult<String, Object>> future = template.send(topic, gson.toJson(data));
        future.addCallback(new KafkaSendCallback<>() {

            /**
             * Called when the {@link ListenableFuture} completes with success.
             * <p>Note that Exceptions raised by this method are ignored.
             *
             * @param result the result
             */
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                //persist in app event as a successful event
                logger.info("notification sent to the event queue");
            }

            /**
             * Called when the send fails.
             *
             * @param ex the exception.
             */
            @Override
            public void onFailure(KafkaProducerException ex) {
                //persist in app event as a failed even
                logger.error("failed to send notification", ex);
            }
        });
    }
}
