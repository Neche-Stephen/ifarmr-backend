package org.ifarmr.payload.response;

import lombok.*;
import org.ifarmr.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LivestockSummaryInfo {

    private String animalName;
    private String quantity;
    private String status;
    private String location;
    private LocalDateTime createdDate;
}
