package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.enums.DeleteType;
import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRequest {

    @NotNull(message = "UserId must be passed")
    private Long userId;
    
    private DeleteType deleteType;
}
