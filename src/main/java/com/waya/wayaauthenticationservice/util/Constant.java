package com.waya.wayaauthenticationservice.util;

public class Constant {

    // JWT Token Constants
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    public static final String TOKEN_PREFIX = "serial ";
    public static final String SECRET_TOKEN = "wayas3cr3t";

    public static final String SUCCESS_MESSAGE = "Successful";
    public static final String ERROR_MESSAGE = "Error";
    public static final String ACCOUNT_CREATION = "http://ACCOUNT-CREATION-SERVICE/";
    public static final String PROFILE_SERVICE = "http://46.101.41.187:8080/";
//    public static final String PROFILE_SERVICE = "http://PROFILE-SERVICE/";
    public static final String WAYA_PROFILE_SERVICE = "http://157.245.84.14:2200/";
//    public static final String WALLET_SERVICE = "http://157.230.223.54:9009/";
    public static final String WALLET_SERVICE = "http://46.101.41.187:9196/";
    public static final String VIRTUAL_ACCOUNT_TOPIC = "virtual-account";
    public static final String WAYAGRAM_PROFILE_TOPIC = "wayagram-profile";
    public static final String WALLET_ACCOUNT_TOPIC = "wallet-account";
    public static final String PROFILE_ACCOUNT_TOPIC = "profile-account";
    public static final String CORPORATE_PROFILE_TOPIC = "corporate-profile-account";
    public static final String CHAT_TOPIC = "wayagram-chat";
    public static final String WAYAPAY = "Wayapay";
    public static final String CHARACTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPZMXNCBVALSKDJFHG1234509687";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "scopes";
    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String TOKEN = "token";
    public static final String ID_IS_INVALID = "Invalid Id provided";
    public static final String MESSAGE = "your OTP is ";

    public static final String MESSAGE_2 = ". Thanks for choosing wayapaychat";

    public static final String OTP_SUCCESS_MESSAGE = "OTP verified successfully";

    public static final String INVALID_OTP = "Invalid OTP";

    public static final String OTP_ERROR_MESSAGE = "OTP has expired";

    public static final String MESSAGE_400 = "Bad Request, invalid format of the request. See response message for more information.";

    public static final String MESSAGE_422 = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.";

    public static final String MESSAGE_404 = "Request is not found";

    public static final String VERIFY_EMAIL_TOKEN_MESSAGE = "Please verify your email with this pin. PIN: ";
    public static final String VERIFY_RESET_TOKEN_MESSAGE = "Please verify your Password Change request with this OTP. OTP: ";
    public static final String EMAIL_TOPIC = "email";

    public static final String SMS_TOPIC = "sms";

    public static final String EMAIL_VERIFICATION_MSG = "Email verified successfully";

    public static final String EMAIL_VERIFICATION_MSG_ERROR = "Token has either expired or is invalid";

    public static final String TWILIO_PROVIDER = "twilio";

    public static final int REFERRAL_CODE_LENGHT = 16;

    public static final String CREATE_PROFILE_SUCCESS_MSG = "profile created. An OTP has been sent to your phone";

    public static final String PROFILE_NOT_EXIST = "profile does not exist";

    public static final String RETRIEVE_DATA_SUCCESS_MSG = "retrieve data successfully ";

    public static final String CATCH_EXCEPTION_MSG = "caught an exception ::: {}";

    public static final String COULD_NOT_PROCESS_REQUEST = "could not process request {}";

    public static final String DUPLICATE_KEY = "duplicate key exception, user id or email might already exist";

    public static final String ID_IS_REQUIRED = "Id is required";

    public static final String ID_IS_UNKNOWN = "Id is unknown";

    public static final String PHONE_NUMBER_REQUIRED = "Phone number is required";

    public static final int LIMIT = 10;

}

