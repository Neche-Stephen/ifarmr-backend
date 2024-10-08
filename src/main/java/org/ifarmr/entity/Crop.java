package org.ifarmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.ifarmr.enums.CropType;
import org.ifarmr.enums.Frequencies;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "crop_tbl")
public class Crop extends BaseClass {

    private String cropName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CropType cropType;

    private LocalDate sowDate;

    private LocalDate harvestDate;

    private String numberOfSeedlings;

    private String costOfSeedlings;

    @Enumerated(EnumType.STRING)
    private Frequencies wateringFrequency;

    @Enumerated(EnumType.STRING)
    private Frequencies fertilizingFrequency;

    private String plantingSeason;

    private String status;

    private String quantity;

    private String location;

    private String pestAndDiseases;

    private String photoUpload;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("crop-user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;
}