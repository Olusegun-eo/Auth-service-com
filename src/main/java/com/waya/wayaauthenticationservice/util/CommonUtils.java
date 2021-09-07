package com.waya.wayaauthenticationservice.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isEmpty(String value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }

    public static Optional<String> objectToJson(Object object){
        try {
            return Optional.of(new ObjectMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Unable to generate json equivalent of the provided object");
            return Optional.empty();
        }
    }

    public static Date convertToDate(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static String getDateAsString(LocalDateTime localDateTime){
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    public static String generatePaymentTransactionId() {
        return new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
    }

    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }



    /**
     * method to generate 6-digit secure token.
     *
     * @return Integer
     */
    public static int generateCode() {

        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextInt(900000) + 100000;
    }

}
