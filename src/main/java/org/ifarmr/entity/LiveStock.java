package org.ifarmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.ifarmr.enums.AnimalType;
import org.ifarmr.enums.Frequencies;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "livestock_tbl")
public class LiveStock extends BaseClass {

    private String animalName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private  AnimalType animalType;

    private String breed;

    private String quantity;

    private String age;

    @Enumerated(EnumType.STRING)
    private Frequencies wateringFrequency;

    @Enumerated(EnumType.STRING)
    private Frequencies feedingSchedule;

    private LocalDate vaccinationSchedule;

    private String healthIssues;

    private String description;


    private String status;

    private String photoUpload;

    private String location;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("livestock-user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;

}
