package com.waya.wayaauthenticationservice.pojo;

import com.waya.wayaauthenticationservice.enums.DeleteType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DeleteRequest {
    private String userId;
    private DeleteType deleteType;
}
