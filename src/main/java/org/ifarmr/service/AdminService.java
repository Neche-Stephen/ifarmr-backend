package org.ifarmr.service;

import org.ifarmr.payload.response.AdminDashboardResponse;
import org.ifarmr.payload.response.FarmerStatisticsResponse;
import org.ifarmr.payload.response.UserListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {

        FarmerStatisticsResponse getFarmerStatistics(String gender, String dateJoinedFrom, String dateJoinedTo, Boolean enabled);
        AdminDashboardResponse getDashboardData(); // New method for fetching dashboard data
        Map<String, Object> getDashboardStats();
        Page<UserListResponse> getOnboardedUsers(Pageable pageable);
        Map<String, Object> getUserStatistics();

}
