package org.ifarmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.ifarmr.enums.ItemType;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory_tbl")
@Builder
public class Inventory extends BaseClass{

    private String name;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private String quantity;

    private String cost;

    private String photoUpload;

    private String location;

    private LocalDate dateAcquired;

    private String  currentState;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("inventory-user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;
}
