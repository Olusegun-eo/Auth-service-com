package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Roles;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTest {

    AuthenticationFilter authenticationFilter = new AuthenticationFilter();

    @Test
    void roleCheck() {
        List<Roles> rolesList = new ArrayList<>();
        Roles role1 = new Roles(1, "User");
        Roles role2 = new Roles(1, "Admin");
        rolesList.add(role1);
        rolesList.add(role2);
        Assert.assertTrue(authenticationFilter.roleCheck(rolesList, "Admin"));
        Assert.assertFalse(authenticationFilter.roleCheck(rolesList, "Agent"));
    }
}