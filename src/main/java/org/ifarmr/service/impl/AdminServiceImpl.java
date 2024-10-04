package org.ifarmr.service.impl;

import org.ifarmr.entity.User;
import org.ifarmr.enums.Gender;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        LocalDateTime now = LocalDateTime.now();
        long newRegistration = farmers.stream()
                .filter(farmer -> ChronoUnit.HOURS.between(farmer.getDateJoined(), now) <= 24)
                .count();

        return new AdminDashboardResponse(totalFarmers, activeFarmers, inactiveFarmers, newRegistration);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();

        List<Long> dailyUserGrowth = new ArrayList<>();
        List<Long> weeklyActiveUsers = new ArrayList<>();
        long totalActiveUsers = 0L;
        double activeUsersPercentageChange = 0.00;

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        // Calculate for each day (Mon-Sun)
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            long totalUsersPerDay = userRepository.findTotalUsersForDate(day).get(0);
            long activeUsersPerDay = userRepository.findActiveUsersForDate(day).get(0);
            dailyUserGrowth.add(totalUsersPerDay);
            weeklyActiveUsers.add(activeUsersPerDay);

            totalActiveUsers += activeUsersPerDay;
        }

        // Calculate today's index based on the current day of the week
        int todayIndex = today.getDayOfWeek().getValue() - 1;
        int yesterdayIndex = (todayIndex == 0) ? 6 : todayIndex - 1;

        long activeUsersToday = weeklyActiveUsers.get(todayIndex);
        long activeUsersYesterday = weeklyActiveUsers.get(yesterdayIndex);
        activeUsersPercentageChange = calculatePercentageChange(activeUsersYesterday, activeUsersToday);

        response.put("dailyUserGrowth", dailyUserGrowth);
        response.put("weeklyActiveUsers", weeklyActiveUsers);
        response.put("totalActiveUsers", totalActiveUsers);
        response.put("activeUsersPercentageChange", activeUsersPercentageChange);

        return response;
    }
    private double calculatePercentageChange(long oldValue, long newValue) {
        if (oldValue == 0) {
            if (newValue == 0) {
                return 0.00;
            }
            return 100.00;
        }
        return ((double) (newValue - oldValue) / oldValue) * 100.00;
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

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Line chart data: Monthly user registration count
        List<Long> monthlyUserRegistration = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int month = 1; month <= 12; month++) {
            LocalDate startOfMonth = LocalDate.of(today.getYear(), month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            long userCountForMonth = userRepository.countUsersByDateJoinedBetween(startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
            monthlyUserRegistration.add(userCountForMonth);
        }

        // Pie chart data: User demographics by gender
        long maleUsers = userRepository.countByGender(Gender.MALE);
        long femaleUsers = userRepository.countByGender(Gender.FEMALE);

        Map<String, Long> genderDistribution = new HashMap<>();
        genderDistribution.put("Male", maleUsers);
        genderDistribution.put("Female", femaleUsers);

        // Add both line chart and pie chart data to the response
        statistics.put("monthlyUserRegistration", monthlyUserRegistration);
        statistics.put("genderDistribution", genderDistribution);

        return statistics;
    }

}