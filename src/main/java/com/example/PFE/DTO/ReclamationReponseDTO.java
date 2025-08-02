package com.example.PFE.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReclamationReponseDTO {
    private Long id;
    private Long reclamationId;
    private Date dateReponse;
    private String message;
    private String reponsePar;
}