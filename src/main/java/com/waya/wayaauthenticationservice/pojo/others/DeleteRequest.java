package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRequest {

    @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING)
    private String userId;
    
    private DeleteType deleteType;
}
