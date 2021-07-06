package com.waya.wayaauthenticationservice.util;

import java.security.SecureRandom;

public final class ProfileServiceUtil {

        private ProfileServiceUtil() {
        }

        /**
         * validates a long.
         *
         * @param val value.
         * @return boolean
         */
        public static boolean validateLong(Long val) {
            try {
                String value = val.toString();
                long result = Long.parseLong(value);

                return result >= 0;
            } catch (Exception exception) {
                return false;
            }
        }

        /**
         * This method validates a number (String value)
         *
         * @param num, number string to be validated
         * @return boolean
         * @author Chisom.Iwowo
         */
        public static Boolean validateNum(String num) {
            try {
                return Long.parseLong(num) >= 0;
            } catch (Exception ex) {
                return false;
            }
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

        /**
         * generates random referral code.
         *
         * @param length length
         * @return String
         */
        public static String generateReferralCode(int length) {
            String characters = Constant.CHARACTERS;
            int characterlength = characters.length();

            StringBuilder stringBuilder = new StringBuilder();

            SecureRandom random = new SecureRandom();

            for (int i = 0; i < length; i++) {
                char chars = characters.charAt(random.nextInt(characterlength));
                stringBuilder.append(chars);
            }
            return stringBuilder.toString();
        }
    }