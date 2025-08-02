package com.example.PFE.Repository;

import com.example.PFE.DTO.ClientDTO;
import com.example.PFE.Model.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {

        // Update this query to match the constructor parameters
        // Correction de la requête - utilise c.loyal au lieu de c.isLoyal
        @Query("SELECT new com.example.PFE.DTO.ClientDTO(c.id, c.name, c.hasUnpaidCredit, c.loyal, c.registrationDate, p.codeAgence) " +
                "FROM Client c JOIN c.placements p WHERE p.codeAgence = :codeAgence")
        List<ClientDTO> findDTOsByAgence(@Param("codeAgence") String codeAgence);


        @Query("SELECT c FROM Client c WHERE c.codeAgence = :codeAgence")
        List<Client> findByCodeAgence(@Param("codeAgence") String codeAgence);
        @Query("SELECT c FROM Client c WHERE c.placements IS EMPTY")
        List<Client> findAllWithoutPlacements();
        @EntityGraph(attributePaths = {"placements"})
        @Query("SELECT c FROM Client c LEFT JOIN FETCH c.placements WHERE c.codeAgence = :codeAgence")
        List<Client> findByCodeAgenceWithPlacements(@Param("codeAgence") String codeAgence);
        @EntityGraph(attributePaths = {"placements"})
        @Query("SELECT c FROM Client c LEFT JOIN FETCH c.placements WHERE c.id = :id")
        Optional<Client> findByIdWithPlacements(@Param("id") Long id);

        @EntityGraph(attributePaths = {"placements"})
        @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.placements")
        List<Client> findAllWithPlacements();


        // Autres méthodes inchangées...
        @EntityGraph(attributePaths = {"placements"})
        @Query("SELECT DISTINCT c FROM Client c JOIN c.placements p WHERE p.codeAgence = :codeAgence")
        List<Client> findByPlacementsCodeAgence(@Param("codeAgence") String codeAgence);
}

