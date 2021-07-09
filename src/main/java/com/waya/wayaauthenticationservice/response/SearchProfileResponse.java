package com.waya.wayaauthenticationservice.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SearchProfileResponse {
    private UUID id;
    private String firstName;
    private String surname;
    private String email;
    private String phoneNumber;
    private String avatar;
    private String userId;
}
