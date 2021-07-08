package com.waya.wayaauthenticationservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaProducerException;
//import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo2;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;

@Service
public class KafkaMessageProducer implements MessageQueueProducer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> template;
    private final Gson gson;
    

//
//    @Autowired
//    private   RestTemplate restClient;
//
//    @Autowired
//    private AppConfig appConfig;
//
//    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
//
//    private static final String SECRET_TOKEN = "wayas3cr3t" ;
//
//    public static final String TOKEN_PREFIX = "serial ";

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
    @SuppressWarnings("unused")
	@Override
    public void send(String topic, Object data) {
    	ProfilePojo2 creds = null;
    	logger.info(String.format("#### -> Producing message -> %s", data.toString()));
		/*
		 * try { creds = new ObjectMapper().readValue(gson.toJson(data),
		 * ProfilePojo2.class); } catch (JsonMappingException e1) {
		 * log.error("An error has occured {}", e1.getMessage()); } catch
		 * (JsonProcessingException e1) { log.error("An error has occured {}",
		 * e1.getMessage()); }
		 */
        ListenableFuture<SendResult<String, Object>> future = template.send(topic, gson.toJson(data));
        //future.addCallback(new KafkaSendCallback<>() {
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

            /**
             * Called when the {@link ListenableFuture} completes with success.
             * <p>Note that Exceptions raised by this method are ignored.
             *
             * @param result the result
             */
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                //persist in app event as a successful event
            	logger.info("Sent message=[" + data.toString() + 
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
                logger.info("notification sent to the event queue");
            }

            /**
             * Called when the send fails.
             *
             * @param ex the exception.
             */
            /*@Override
            public void onFailure(KafkaProducerException ex) {
                //persist in app event as a failed even
            	logger.error("Unable to send message=[" 
                        + data + "] due to : " + ex.getMessage());
                logger.error("failed to send notification", ex);
            }*/

			@Override
			public void onFailure(Throwable ex) {
				//persist in app event as a failed even
            	logger.error("Unable to send message=[" 
                        + data + "] due to : " + ex.getMessage());
                logger.error("failed to send notification", ex.getMessage());
                logger.error("Full Error", ex);
				
			}
        });
//        Optional<Users> foundUser = userRepo.findByEmail(creds.getEmail());
//
//        foundUser.ifPresent(users -> CompletableFuture.runAsync(() -> createProfile(users)));

    }
    



//    private void createProfile(Users user){
//        try{
//
//            log.info("creating user profile ... {}",user);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            Map<String, Object> map = new HashMap<>();
//            map.put("email", user.getEmail());
//            map.put("firstName", user.getFirstName());
//            map.put("phoneNumber", user.getPhoneNumber());
//            map.put("referralCode", user.getReferenceCode());
//            map.put("surname", user.getSurname());
//            map.put("userId", String.valueOf(user.getId()));
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
//            ResponseEntity<String> response = restClient.postForEntity(appConfig.getUrl(), entity, String.class);
//            if (response.getStatusCode() == CREATED) {
//                log.info("User profile created {}", response.getBody());
//            } else {
//                log.info("User profile  Request Failed with body:: {}", response.getStatusCode());
//            }
//        }catch (Exception e){
//            log.error("User profile   Exception: ", e);
//        }
//    }

}
