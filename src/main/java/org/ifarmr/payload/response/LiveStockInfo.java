package org.ifarmr.payload.response;

import lombok.*;
import org.ifarmr.enums.AnimalType;
import org.ifarmr.enums.Frequencies;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveStockInfo {
    private String animalName;
    private AnimalType animalType;
    private String breed;
    private String quantity;
    private String age;
    private Frequencies wateringFrequency;
    private Frequencies feedingSchedule;
    private LocalDate vaccinationSchedule;
    private String healthIssues;
    private String description;
    private String status;
    private String photoUpload;
    private String location;
}
