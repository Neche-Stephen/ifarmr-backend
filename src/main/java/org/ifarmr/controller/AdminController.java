package org.ifarmr.controller;

import org.ifarmr.payload.response.AdminDashboardResponse;
import org.ifarmr.payload.response.FarmerStatisticsResponse;
import org.ifarmr.payload.response.UserListResponse;
import org.ifarmr.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/farmer-statistics")
    public FarmerStatisticsResponse getFarmerStatistics(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String dateJoinedFrom,
            @RequestParam(required = false) String dateJoinedTo,
            @RequestParam(required = false) Boolean enabled
    ) {
        return adminService.getFarmerStatistics(gender, dateJoinedFrom, dateJoinedTo, enabled);
    }
//Api For Admin to get the dashboard-data
    @GetMapping("/dashboard-data")
    public AdminDashboardResponse getDashboardData() {
        return adminService.getDashboardData();
    }

    @GetMapping( "/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> dashboardStats = adminService.getDashboardStats();
        return ResponseEntity.ok(dashboardStats);
    }

    @GetMapping("/all-users")
    public ResponseEntity<Page<UserListResponse>> getOnboardedUsers(){
        int page = 0;
        int size = 10;

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserListResponse> users = adminService.getOnboardedUsers(pageRequest);

        return ResponseEntity.ok(users);
    }
}