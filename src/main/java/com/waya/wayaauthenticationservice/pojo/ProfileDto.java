package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProfileDto {

    private UUID id;
    private String email;
    private String firstName;
    private String surname;
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
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String referral;
    private boolean corporate;
    private OtherDetailsDto otherDetails;
}
