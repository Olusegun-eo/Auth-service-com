package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletPojo {

    @NotNull(message="Boolean Corporate cannot be null")
    private boolean corporate;

    @NotNull(message="UserId cannot be null")
    private int userId;

}