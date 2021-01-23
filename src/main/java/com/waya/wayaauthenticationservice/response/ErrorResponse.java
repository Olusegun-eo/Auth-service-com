package com.waya.wayaauthenticationservice.response;

import org.apache.logging.log4j.util.Strings;

import static com.waya.wayaauthenticationservice.util.Constant.ERROR_MESSAGE;

public class ErrorResponse extends ResponseHelper {

    public ErrorResponse(String message){
        super(false, message, Strings.EMPTY);
    }

    public ErrorResponse(){
        super(false, ERROR_MESSAGE, Strings.EMPTY);
    }

}
