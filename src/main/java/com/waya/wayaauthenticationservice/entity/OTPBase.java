package com.waya.wayaauthenticationservice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "m_otp_base")
public class OTPBase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer code;

    private String email;

    private boolean valid = true;

    private String phoneNumber;

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
