package org.ifarmr.payload.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private int status;
    private String message;
}