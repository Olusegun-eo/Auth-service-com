package com.waya.wayaauthenticationservice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "please enter a valid email")
    @Column(unique = true)
    @Email(message = "please enter a valid email")
    private String email;

    @NotNull(message = "please enter your firstName")
    private String firstName;

    @NotNull(message = "please enter your surname")
    private String surname;

    @NotNull(message = "please enter your phone number")
    private String phoneNumber;

    private String organisationName;

    private String middleName;

    private String profileImage;

    private String dateOfBirth;

    private String gender;

    private String age;

    private String district;

    private String address;

    private String city;

    private String state;

    private boolean deleted;

    @Column(nullable = false, unique = true)
    private String userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String referral;

    private boolean corporate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "otherdetails_id", referencedColumnName = "id")
    private OtherDetails otherDetails;
}