package com.waya.wayaauthenticationservice.pojo.access;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserAccessResponse {

    private Long id;
    private Long managerId;
    private String permissionName;
    private String roleName;

}
