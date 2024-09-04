package org.ifarmr.payload.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentActivityDto {
    private String icon;
    private String title;
    private String description;
    private String timeAgo;
    private String date;
}
