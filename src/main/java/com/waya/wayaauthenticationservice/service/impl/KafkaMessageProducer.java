package com.waya.wayaauthenticationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.CreateWayagram;
import com.waya.wayaauthenticationservice.pojo.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.ProfilePojo2;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.MessageQueueProducer;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

@Service
public class KafkaMessageProducer implements MessageQueueProducer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> template;
    private final Gson gson;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private RestTemplate restTemplate;
    
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    
    private static final String SECRET_TOKEN = "wayas3cr3t" ;
    
    public static final String TOKEN_PREFIX = "serial ";

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
    	ProfilePojo2 creds = null;
		try {
			creds = new ObjectMapper().readValue(gson.toJson(data), ProfilePojo2.class);
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
        Optional<Users> foundUser = userRepo.findByEmail(creds.getEmail());
        String mUserId = String.valueOf(foundUser.get().getId());
        
        String token = generateToken(foundUser.get());
        
        CreateWayagram createWayagram = new CreateWayagram();
        createWayagram.setNotPublic(false);
        createWayagram.setUser_id(mUserId);
        createWayagram.setUsername(foundUser.get().getEmail());
        try {
			createWayagram(createWayagram, token);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private ResponseEntity<?> createWayagram(CreateWayagram createWayagram,  String token) throws URISyntaxException {
		
		System.out.println("::::::GET WALLET::::::"+createWayagram.getUser_id());
//		final String baseUrl = "http://46.101.41.187:9196/api/v1/wallet-transactions";
		final String baseUrl = "http://157.245.84.14:1000/profile/create";
		System.out.println("::::::GET WALLET2::::::");
//        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);  
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println("::::::GET WALLET 3::::::"+token);
     // request body parameters
     // build the request
        HttpEntity request = new HttpEntity(createWayagram,headers);
        
     // build the request
        System.out.println(":::::loading transfer:::::");
        
//        ResponseEntity<String> walletResp = restTemplate.postForEntity(uri, entity, String.class);
        ResponseEntity<String> response = restTemplate.exchange(
        		baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        System.out.println(":::Response:::"+response.getBody());
        System.out.println(":::Response:::"+response.getStatusCodeValue());
        return response;
	}
    
    public String generateToken(Users userResponse) {
    	try {
    		System.out.println("::::::GENERATE TOKEN:::::");
        	String token = Jwts.builder().setSubject(userResponse.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(SignatureAlgorithm.HS512, SECRET_TOKEN).compact();
        	System.out.println(":::::Token:::::"+TOKEN_PREFIX+token);
        	return TOKEN_PREFIX+token;
    	} catch (Exception e) {
    		
    		System.out.println(e.fillInStackTrace());
    		throw new RuntimeException(e.fillInStackTrace());
    	}
    	
    }
}
