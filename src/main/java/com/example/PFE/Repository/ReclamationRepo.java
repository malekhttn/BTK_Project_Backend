package com.example.PFE.Repository;

import com.example.PFE.Model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamationRepo extends JpaRepository<Reclamation, Long>, JpaSpecificationExecutor<Reclamation> {
    Page<Reclamation> findByPlacementId(Long placementId, Pageable pageable);
    Page<Reclamation> findByStatut(StatutReclamation statut, Pageable pageable);
    Page<Reclamation> findByTypeProbleme(ProblemePlacement typeProbleme, Pageable pageable);
    Page<Reclamation> findByStatutAndPlacementId(StatutReclamation statut, Long placementId, Pageable pageable);
    Page<Reclamation> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titre, String description, Pageable pageable);
    List<Reclamation> findByStatut(StatutReclamation statut);
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.lu = false AND r.dateCreation > :date")
    Long countByLuFalseAndDateCreationAfter(@Param("date") Date date);

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.lu = false AND r.dateCreation <= :date")
    Long countByLuFalseAndDateCreationBefore(@Param("date") Date date);

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.statut = :statut")
    Long countByStatut(@Param("statut") StatutReclamation statut);


        @EntityGraph(attributePaths = {"reponses"})
        @Query("SELECT r FROM Reclamation r WHERE " +
                "(:statut IS NULL OR r.statut = :statut) AND " +
                "(:placementId IS NULL OR r.placement.id = :placementId) AND " +
                "(:search IS NULL OR LOWER(r.titre) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Reclamation> findAllWithReponses(
                @Param("statut") StatutReclamation statut,
                @Param("placementId") Long placementId,
                @Param("search") String search,
                Pageable pageable);
    @EntityGraph(attributePaths = {"reponses", "placement"})
    @Query("SELECT r FROM Reclamation r WHERE " +
            "(:statut IS NULL OR r.statut = :statut) AND " +
            "(:placementId IS NULL OR r.placement.id = :placementId) AND " +
            "(:search IS NULL OR LOWER(r.titre) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reclamation> findAllWithReponsesAndPlacement(
            @Param("statut") StatutReclamation statut,
            @Param("placementId") Long placementId,
            @Param("search") String search,
            Pageable pageable);





    @EntityGraph(attributePaths = {"reponses", "placement"})
    @Query("SELECT r FROM Reclamation r WHERE " +
            "r.placement.codeAgence = :codeAgence AND " +
            "(:statut IS NULL OR r.statut = :statut) AND " +
            "(:placementId IS NULL OR r.placement.id = :placementId) AND " +
            "(:search IS NULL OR LOWER(r.titre) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reclamation> findAllByPlacementCodeAgence(
            @Param("codeAgence") String codeAgence,
            @Param("statut") StatutReclamation statut,
            @Param("placementId") Long placementId,
            @Param("search") String search,
            Pageable pageable);




    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.lu = false AND r.dateCreation > :date")
    long countByLuFalseAndDateCreationAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.lu = false AND r.dateCreation <= :date")
    long countByLuFalseAndDateCreationBefore(@Param("date") LocalDateTime date);
    @EntityGraph(attributePaths = {"reponses", "placement"})
    @Query("SELECT r FROM Reclamation r WHERE r.id = :id")
    Optional<Reclamation> findByIdWithReponses(@Param("id") Long id);

}