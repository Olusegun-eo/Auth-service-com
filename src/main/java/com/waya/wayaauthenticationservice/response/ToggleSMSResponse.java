package com.waya.wayaauthenticationservice.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ToggleSMSResponse {
    private UUID id;
    private String phoneNumber;
    private boolean active = true;
}
