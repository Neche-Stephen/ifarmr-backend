package org.ifarmr.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long id;

    private String  itemType;

    private String  name;

    private String quantity;

    private String cost;

    private String location;

    private LocalDate dateAcquired;

    private String  currentState;

    private String photoUrl;
}
