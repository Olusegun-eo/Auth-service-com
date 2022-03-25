package com.waya.wayaauthenticationservice.service;

public interface DashboardService {

    long countActiveUser();
    long countActiveUsersCooperate();
    long countInactiveUsers();
    long countInactiveUsersCooperate();
}
