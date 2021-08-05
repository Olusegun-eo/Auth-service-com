package com.waya.wayaauthenticationservice.pojo.access;

import lombok.Data;

@Data
public class UserAccessResponse {

    private Long id;
    private Long managerId;
    private String permissionName;
    private String roleName;

}
