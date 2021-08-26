package com.waya.wayaauthenticationservice.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HelperUtils {

    public static Pattern phoneNumPattern = Pattern.compile("^[0-9]*$");
    public static Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

    public static String generateRandomPassword() {

        int length = generateRandomNumber(11, 15);
        int randNumOrigin = generateRandomNumber(58, 34);
        int randNumBound = generateRandomNumber(154, 104);

        SecureRandom random = new SecureRandom();
        return random.ints(randNumOrigin, randNumBound + 1)
                .filter(i -> Character.isAlphabetic(i) || Character.isDigit(i))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    public static String generateRandomNumber(int length) {

        int randNumOrigin = generateRandomNumber(58, 34);
        int randNumBound = generateRandomNumber(354, 104);

        SecureRandom random = new SecureRandom();
        return random.ints(randNumOrigin, randNumBound + 1)
                .filter(Character::isDigit)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    public static boolean isEmail(String value) {
        if(value == null)
            return false;

        Matcher matcher = emailPattern.matcher(value);
        return matcher.matches();
    }

    /**
     * Return true if value passed is null as this helper method is used to validate optional inputs
     * @param value value of the email or PhoneNumber
     * @return boolean
     */
    public static boolean isEmailOrPhoneNumber(String value) {
    	if(value == null)
            return true;
    	
    	if(value.startsWith("+"))
    		value = value.substring(1);
    	
    	value = value.replaceAll("\\s+", "").trim();
        boolean val = phoneNumPattern.matcher(value).find() 
        		&& value.startsWith("234")
                && value.length() == 13;
        Matcher emailMatcher = emailPattern.matcher(value);
        return emailMatcher.matches() || val;
    }

    public static boolean isNumericOnly(String value) {
        return value.matches("^[0-9]*$");
    }

    public static boolean validateStringTextOnly(String value) {
        return value.matches("^([^0-9]*)$");
    }

    public static int generateRandomNumber(int max, int min) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
