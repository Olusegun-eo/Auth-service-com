package com.waya.wayaauthenticationservice.response;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SMSChargeResponse {
    private UUID id;
    private BigDecimal fee;
    private boolean active = true;
}
