package com.waya.wayaauthenticationservice.response;


import static com.waya.wayaauthenticationservice.util.Constant.SUCCESS_MESSAGE;

public class SuccessResponse extends ResponseHelper{

    public SuccessResponse(String message, Object data){
        super(true, message, data);
    }

    public SuccessResponse(Object data){
        super(true, SUCCESS_MESSAGE, data);
    }

}
