package org.ifarmr.payload.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.ifarmr.enums.AnimalType;
import org.ifarmr.enums.Frequencies;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveStockRequest {

//    @NotNull(message = "Animal name is required")
//    @Size(min = 1, max = 30, message = "Animal name must be between 1 and 30 characters")
//    @NotNull
    private String animalName;

    @Enumerated(EnumType.STRING)
    private AnimalType animalType;

    private String breed;

    private String quantity;

    private String age;

    @NotNull(message = "This is required")
    private MultipartFile photoUpload;

    @Enumerated(EnumType.STRING)
    private Frequencies wateringFrequency;

    @Enumerated(EnumType.STRING)
    private Frequencies feedingSchedule;

    private LocalDate vaccinationSchedule;

    private String healthIssues;

    private String description;


    private String status;

    private String location;

}
