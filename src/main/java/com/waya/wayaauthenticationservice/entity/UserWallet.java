package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.enums.WalletAccountType;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@ToString
@Table(name = "m_users_wallet")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String accountBank;

    private String accountName;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(columnDefinition = "enum('INTERNAL','VIRTUAL')")
    private WalletAccountType accountType;

    private String accountId;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
