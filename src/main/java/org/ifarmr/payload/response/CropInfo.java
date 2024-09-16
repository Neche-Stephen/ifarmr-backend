package org.ifarmr.payload.response;

import lombok.*;
import org.ifarmr.enums.CropType;
import org.ifarmr.enums.Frequencies;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropInfo {
    private String cropName;
    private CropType cropType;
    private LocalDate sowDate;
    private LocalDate harvestDate;
    private String numberOfSeedlings;
    private String costOfSeedlings;
    private Frequencies wateringFrequency;
    private Frequencies fertilizingFrequency;
    private String pestAndDiseases;
    private String photoUpload;
    private String quantity;
    private String location;
    private String status;
    private String plantingSeason;
    private String description;

}