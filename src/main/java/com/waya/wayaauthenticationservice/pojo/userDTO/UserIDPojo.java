package com.waya.wayaauthenticationservice.pojo.userDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;

public class UserIDPojo {

    @JsonProperty("user_id")
    @CustomValidator(message = "UserId Passed must be numeric", type = Type.NUMERIC_STRING)
    private String userId;

    public UserIDPojo(String userId) {
        this.userId = userId;
    }
}
