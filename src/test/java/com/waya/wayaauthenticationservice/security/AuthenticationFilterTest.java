package com.waya.wayaauthenticationservice.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.waya.wayaauthenticationservice.entity.Roles;

class AuthenticationFilterTest {

    AuthenticationFilter authenticationFilter = new AuthenticationFilter();

    @Test
    void roleCheck() {
        List<Roles> rolesList = new ArrayList<>();
        Roles role1 = new Roles(1, "ROLE_USER");
        Roles role2 = new Roles(1, "ROLE_ADMIN");
        rolesList.add(role1);
        rolesList.add(role2);
        Assert.assertTrue(authenticationFilter.roleCheck(rolesList, "ROLE_USER"));
        Assert.assertFalse(authenticationFilter.roleCheck(rolesList, "ROLE_ADMIN"));
    }
}