package org.ifarmr.service;

import org.ifarmr.payload.response.AdminDashboardResponse;
import org.ifarmr.payload.response.FarmerStatisticsResponse;

public interface AdminService {

        FarmerStatisticsResponse getFarmerStatistics(String gender, String dateJoinedFrom, String dateJoinedTo, Boolean enabled);
        AdminDashboardResponse getDashboardData(); // New method for fetching dashboard data
}
