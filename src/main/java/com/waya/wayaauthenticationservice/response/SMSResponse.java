package com.waya.wayaauthenticationservice.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SMSResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private boolean active = true;
    private Long userId;
}
