package com.waya.wayaauthenticationservice.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private String id;
    private String email;
    private String firstName;
    private String surname;
    private String middleName;
    private String profileImage;
    private String dateOfBirth;
    private String gender;
    private String district;
    private String address;
    private String phoneNumber;
    private String userId;
    private String referenceCode;
    private boolean smsAlertConfig;
    private boolean pinCreated;
    private String city;
    private boolean corporate;

    private OtherdetailsResponse otherDetails;

}