package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.service.DashboardService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "AUTH", description = "User Dashboard Service API")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @ApiOperation(value = "${api.dashboard.users-active.description}",
            notes = "${api.dashboard.users-active.notes}", tags = {"DASHBOARD RESOURCE"})
    @GetMapping("/active-users")
    public long countActiveUsers() {
        return dashboardService.countActiveUser();
    }

    @ApiOperation(value = "${api.dashboard.cooperate-active.description}",
            notes = "${api.dashboard.cooperate-active.notes}", tags = {"DASHBOARD RESOURCE"})
    @GetMapping("/active-cooperate-users")
    public long countActiveCooperateUsers() {
        return dashboardService.countActiveUsersCooperate();
    }

    @ApiOperation(value = "${api.dashboard.users-in-active.description}",
            notes = "${api.dashboard.users-in-active.notes}", tags = {"DASHBOARD RESOURCE"})
    @GetMapping("/in-active-users")
    public long countInActiveUsers() {
        return dashboardService.countInactiveUsers();
    }

    @ApiOperation(value = "${api.dashboard.cooperate-in-active.description}",
            notes = "${api.dashboard.cooperate-in-active.notes}", tags = {"DASHBOARD RESOURCE"})
    @GetMapping("/in-active-cooperate-users")
    public long countInActiveCooperateUsers() {
        return dashboardService.countInactiveUsersCooperate();
    }
}
