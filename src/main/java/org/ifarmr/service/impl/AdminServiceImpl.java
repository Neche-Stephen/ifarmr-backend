package org.ifarmr.service.impl;

import org.ifarmr.entity.User;
import org.ifarmr.payload.response.AdminDashboardResponse;
import org.ifarmr.payload.response.FarmerStatisticsResponse;
import org.ifarmr.payload.response.UserListResponse;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.AdminService;
import org.ifarmr.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public FarmerStatisticsResponse getFarmerStatistics(String gender, String dateJoinedFrom, String dateJoinedTo, Boolean enabled) {
        List<User> farmers = userRepository.findAll(); // Fetch all farmers.

        // Filter based on gender

        if (gender != null && !gender.isEmpty()) {
            farmers = farmers.stream()
                    .filter(farmer -> farmer.getGender().toString().equalsIgnoreCase(gender))
                    .collect(Collectors.toList());
        }

        // Filter based on date joined

        if (dateJoinedFrom != null && dateJoinedTo != null) {
            LocalDate fromDate = LocalDate.parse(dateJoinedFrom);
            LocalDate toDate = LocalDate.parse(dateJoinedTo);
            farmers = farmers.stream()
                    .filter(farmer -> !farmer.getDateJoined().toLocalDate().isBefore(fromDate) && !farmer.getDateJoined().toLocalDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        // Filter based on enabled status

        if (enabled != null) {
            farmers = farmers.stream()
                    .filter(farmer -> farmer.isEnabled() == enabled)
                    .collect(Collectors.toList());
        }

        // Calculate statistics

        long totalFarmers = farmers.size();
        long activeFarmers = farmers.stream().filter(User::isEnabled).count();
        long inactiveFarmers = totalFarmers - activeFarmers;

        return new FarmerStatisticsResponse(totalFarmers, activeFarmers, inactiveFarmers);
    }

    @Override
    public AdminDashboardResponse getDashboardData() {
        List<User> farmers = userRepository.findAll(); // Fetch all farmers

        long totalFarmers = farmers.size();
        long activeFarmers = farmers.stream().filter(User::isEnabled).count();
        long inactiveFarmers = totalFarmers - activeFarmers;

        return new AdminDashboardResponse(totalFarmers, activeFarmers, inactiveFarmers);
    }

    @Override
    public Page<UserListResponse> getOnboardedUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> UserListResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .gender(user.getGender())
                .dateJoined(DateUtil.formatDate(user.getDateJoined()))
                .lastActive(DateUtil.formatDate(user.getLastActive()))


                .build());
    }
}