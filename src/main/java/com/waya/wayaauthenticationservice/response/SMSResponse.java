package com.waya.wayaauthenticationservice.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SMSResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private boolean active;
    private Long userId;
}
