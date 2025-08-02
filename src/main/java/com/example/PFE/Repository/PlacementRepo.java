package com.example.PFE.Repository;
import com.example.PFE.DTO.PlacementDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PFE.Model.Placement;

import java.util.List;

@Repository
public interface PlacementRepo extends JpaRepository<Placement, Long> {
//    @Query("SELECT p FROM Placement p LEFT JOIN FETCH p.client")
//    List<Placement> findAllWithClient();
@EntityGraph(attributePaths = {"client"})
@Query("SELECT p FROM Placement p")
List<Placement> findAllWithClient();

    @EntityGraph(attributePaths = {"client"})
    List<Placement> findByCodeAgence(String codeAgence);

//    @Query("SELECT new com.example.PFE.DTO.PlacementDTO(p.id, p.codeAgence, ..., c.id, c.name) " +
//            "FROM Placement p LEFT JOIN p.client c WHERE p.codeAgence = :codeAgence")
//    List<PlacementDTO> findDTOsByCodeAgence(@Param("codeAgence") String codeAgence);
}

