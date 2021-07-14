package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.enums.DeleteType;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRequest {
    private String userId;
    private DeleteType deleteType;
}
