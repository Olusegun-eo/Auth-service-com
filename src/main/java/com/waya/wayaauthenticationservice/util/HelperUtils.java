package com.waya.wayaauthenticationservice.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class HelperUtils {

    public static Pattern numericPattern = Pattern.compile("^[0-9]*$");
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

    public static int generateRandomNumber(int max, int min) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
