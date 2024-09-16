package org.ifarmr.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.ifarmr.enums.CropType;
import org.ifarmr.enums.Frequencies;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropRequest {

    @NotNull(message = "Crop name is required")
    @Size(min = 1, max = 30, message = "Crop name must be between 1 and 30 characters")
    private String cropName;

    @NotNull(message = "Crop type is required")
    @Enumerated(EnumType.STRING)
    private CropType cropType;

    @NotNull(message = "Harvest Date is required")
    //@FutureOrPresent(message = "Harvest date must be today or in the future")
    @Schema(description = "The harvest date and time example = \"2024-12-12")
    private LocalDate harvestDate;

    @NotNull(message = "Number of Seedlings is required")
    private String numberOfSeedlings;

    @NotNull(message = "Cost of Seedlings is required")
    private String costOfSeedlings;

//    @NotNull(message = "Watering Frequencies is required")
@Enumerated(EnumType.STRING)
    private Frequencies wateringFrequency;

//    @NotNull(message = "Fertilizing Frequencies is required")
@Enumerated(EnumType.STRING)
    private Frequencies fertilizingFrequency;

    @NotNull(message = "Pest and Diseases information is required")
    private String pestAndDiseases;

    @NotNull(message = "Photo Upload is required")
    private MultipartFile photoUpload;

    @NotNull(message = "Status is required")
    private String status;


    private String description;
    private String plantingSeason;

    // Extra fields for the dashboard
    private String quantity;
    private String location;

    @NotNull(message = "Sow date is required")
    //@FutureOrPresent(message = "Sow date must be today or in the future")
    @Schema(description = "The sow date and time, example = \"2024-08-28")
    private LocalDate sowDate;
}
