package com.example.PFE.DTO;

import com.example.PFE.Model.ProblemePlacement;
import com.example.PFE.Model.StatutReclamation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReclamationDTO {
    private Long id;
    private Long placementId;
    private String titre;
    private ProblemePlacement typeProbleme;
    private Boolean lu;
    private String description;
    private StatutReclamation statut;
    private Date dateCreation;
    private List<ReclamationReponseDTO> reponses;
}