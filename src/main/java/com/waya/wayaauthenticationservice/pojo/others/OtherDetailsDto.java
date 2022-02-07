package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.util.UUID;

@Data
public class OtherDetailsDto {
    private UUID id;
    private String organisationName;
    private String organisationType;
    private String businessType;
}
