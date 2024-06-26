package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.enums.FraudType;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "m_auth_fraud_base")
@AllArgsConstructor
@NoArgsConstructor
public class FraudAction extends AuditModel implements Serializable {

    private static final long serialVersionUID = -2675537776836756234L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private FraudType fraudType;

    private Integer attempts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
