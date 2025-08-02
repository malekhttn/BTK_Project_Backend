package com.example.PFE.IService;

import com.example.PFE.Model.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReclamationIService {
    Reclamation createReclamation(Reclamation reclamation);
    Page<Reclamation> getAllReclamations(Pageable pageable, StatutReclamation statut, Long id, String search);
    Reclamation getReclamationById(Long id);
    Reclamation updateReclamation(Long id, Reclamation reclamation);
    void deleteReclamation(Long id);
    void deleteResponse(Long id);
    ReclamationReponse addReponseToReclamation(Long reclamationId, ReclamationReponse reponse);
    List<ReclamationReponse> getReponsesByReclamation(Long reclamationId);
    Reclamation changeStatus(Long id, StatutReclamation nouveauStatut);
    ReclamationStats getReclamationStats();
    ReclamationReponse updateResponse(Long id, ReclamationReponse reponse);
     Page<Reclamation> getReclamationsByAgence(
            String codeAgence,
            Pageable pageable,
            StatutReclamation statut,
            Long placementId,
            String search);

    long countNewToday();
    long countUnread ();

    long countByStatut(StatutReclamation statut);

    long countTotal() ;}