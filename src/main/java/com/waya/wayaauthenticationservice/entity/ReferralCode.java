package com.waya.wayaauthenticationservice.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String referalCode;

    @Column(unique = true)
    private String userId;

    @OneToOne
    private Profile profile;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ReferralCode(String referalCode, Profile profile, String userId) {
        this.referalCode = referalCode;
        this.profile = profile;
        this.userId = userId;
    }
}
