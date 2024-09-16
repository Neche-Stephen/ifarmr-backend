package org.ifarmr.payload.response;

import lombok.*;
import org.ifarmr.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropSummaryInfo {

    private String cropName;
    private String status;
    private String quantity;
    private LocalDate sowDate;
    private LocalDate harvestDate;

}
