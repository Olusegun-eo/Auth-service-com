package com.waya.wayaauthenticationservice.streams;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RecipientsEmail {
    private String email;
    private String fullName;
}
