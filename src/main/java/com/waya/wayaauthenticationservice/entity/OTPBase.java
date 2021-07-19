package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "m_otp_base")
public class OTPBase implements Serializable {

	private static final long serialVersionUID = 5704318411204309502L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer code;

    private String email;

    @SuppressWarnings("unused")
	private boolean valid;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private OTPRequestType requestType;

    private LocalDateTime expiryDate;

    private String keycloakId;

    public void setExpiryDate(int minutes) {
        LocalDateTime localDateTime = LocalDateTime.now();
        this.expiryDate = localDateTime.plusMinutes(minutes);
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isValid() {
        return this.expiryDate.isAfter(LocalDateTime.now());
    }
    
}
