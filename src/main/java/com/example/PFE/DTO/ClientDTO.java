package com.example.PFE.DTO;

import com.example.PFE.Model.Placement;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    private Long id;
    private String name;
    private boolean hasUnpaidCredit;
    private LocalDate registrationDate;
    private List<PlacementDTO> placements;
    private String codeAgence;
    @JsonProperty("isLoyal") // Force JSON property name
    private boolean loyal; // Renamed to match entity
    // Constructor for DTO projection (6 parameters)
    // In ClientDTO.java
// Modify the constructors to properly include isLoyal
    public ClientDTO(Long id, String name, boolean hasUnpaidCredit,
                     boolean loyal, LocalDate registrationDate, String codeAgence) {
        this.id = id;
        this.name = name;
        this.hasUnpaidCredit = hasUnpaidCredit;
        this.loyal = loyal;
        this.registrationDate = registrationDate;
        this.codeAgence = codeAgence;
        this.placements = null;
    }

    // Constructor without placements (5 parameters - if still needed)
    public ClientDTO(Long id, String name, boolean hasUnpaidCredit,
                     boolean isLoyal, LocalDate registrationDate) {
        this(id, name, hasUnpaidCredit, isLoyal, registrationDate, null);
    }
}