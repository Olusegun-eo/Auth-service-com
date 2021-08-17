package com.waya.wayaauthenticationservice.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@SuppressWarnings("all")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "m_sms_alert_config")
public class SMSAlertConfig extends AuditModel  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name="phone_number", unique = true, nullable = false)
    private String phoneNumber;

    private Long userId;

    private boolean active = true;
}
