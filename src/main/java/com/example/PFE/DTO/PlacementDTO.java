package com.example.PFE.DTO;

import com.example.PFE.Model.PlacementStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlacementDTO {
    private Long id;
    private String codeAgence;
    private String typeClient;
    private String pin;
    private String naturePlacement;
    private Double montant;
    private Double tauxPropose;
    private Integer duree;
    private String origineFonds;
    private Boolean engagementRelation;
    private Double tauxCredit;
    private Boolean nantissement;
    private PlacementStatus statut;
    private String message;
    private Long clientId;
    private String clientName;
    private List<ReclamationDTO> reclamations;
}