package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

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
    private String referral;
    private boolean corporate;
    private OtherDetailsDto otherDetails;
}
