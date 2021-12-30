package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class SuperAdminCreatUserRequest {
    private String fullName;
    private String email;
    private String roleId;
    private List<FunctionsPojo> functions;
    private boolean isAdmin = false;
    @JsonIgnore
    private boolean isWayaAdmin = false;
}

@Data
class FunctionsPojo{
    private Long id;
    private String name;
}
