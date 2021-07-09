package com.waya.wayaauthenticationservice.response;

import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

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
