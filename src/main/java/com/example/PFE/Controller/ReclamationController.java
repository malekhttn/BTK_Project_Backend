package com.example.PFE.Controller;

import com.example.PFE.DTO.ReclamationDTO;
import com.example.PFE.DTO.ReclamationReponseDTO;
import com.example.PFE.Exception.ResourceNotFoundException;
import com.example.PFE.Model.*;
import com.example.PFE.IService.ReclamationIService;
import com.example.PFE.ServiceImpl.PlacementServiceImpl;
import com.example.PFE.ServiceImpl.UserServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin
public class ReclamationController {
    private final ReclamationIService reclamationService;
    private final PlacementServiceImpl placementService;
    private final UserServiceImpl userService;

    public ReclamationController(ReclamationIService reclamationService,
                                 PlacementServiceImpl placementService,
                                 UserServiceImpl userService) {
        this.reclamationService = reclamationService;
        this.placementService = placementService;
        this.userService = userService;
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            reclamationService.deleteReclamation(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<ReclamationDTO> create(@RequestBody ReclamationDTO reclamationDTO) {
        Placement placement = placementService.getPlacementById(reclamationDTO.getPlacementId());
        if (placement == null) {
            throw new IllegalArgumentException("Placement not found with id: " + reclamationDTO.getPlacementId());
        }

        Reclamation reclamation = new Reclamation();
        reclamation.setPlacement(placement);
        reclamation.setTitre(reclamationDTO.getTitre());
        reclamation.setTypeProbleme(reclamationDTO.getTypeProbleme());
        reclamation.setDescription(reclamationDTO.getDescription());
        reclamation.setStatut(reclamationDTO.getStatut());

        Reclamation created = reclamationService.createReclamation(reclamation);
        return ResponseEntity.ok(convertToDTO(created));
    }

    @GetMapping
    public ResponseEntity<Page<ReclamationDTO>> getAll(
            Pageable pageable,
            @RequestParam(required = false) StatutReclamation statut,
            @RequestParam(required = false) Long placementId,
            @RequestParam(required = false) String search) {

        User currentUser = userService.getCurrentUser();
        Page<Reclamation> reclamations;

        if (currentUser.getRole() == User.Role.CHEF_AGENCE) {
            reclamations = reclamationService.getReclamationsByAgence(
                    currentUser.getCodeAgence(), pageable, statut, placementId, search);
        } else {
            reclamations = reclamationService.getAllReclamations(
                    pageable, statut, placementId, search);
        }

        Page<ReclamationDTO> dtoPage = reclamations.map(this::convertToDTO);
        return ResponseEntity.ok(dtoPage);
    }

//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
//    public ResponseEntity<ReclamationDTO> getById(@PathVariable Long id) {
//        Reclamation reclamation = reclamationService.getReclamationById(id);
//        return ResponseEntity.ok(convertToDTO(reclamation));
//    }
@GetMapping("/{id}")
@Transactional// Add this annotation
public ResponseEntity<ReclamationDTO> getById(@PathVariable Long id) {
    Reclamation reclamation = reclamationService.getReclamationById(id);
    // Initialize the collection if needed
    if (reclamation.getReponses() != null) {
        reclamation.getReponses().size(); // This forces initialization
    }
    return ResponseEntity.ok(convertToDTO(reclamation));
}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<ReclamationDTO> update(@PathVariable Long id, @RequestBody ReclamationDTO reclamationDTO) {
        Reclamation reclamation = reclamationService.getReclamationById(id);

        reclamation.setTitre(reclamationDTO.getTitre());
        reclamation.setTypeProbleme(reclamationDTO.getTypeProbleme());
        reclamation.setDescription(reclamationDTO.getDescription());
        reclamation.setStatut(reclamationDTO.getStatut());

        Reclamation updated = reclamationService.updateReclamation(id, reclamation);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @PostMapping("/{id}/reponses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReclamationReponseDTO> addResponse(
            @PathVariable Long id,
            @RequestBody ReclamationReponseDTO reponseDTO) {

        ReclamationReponse reponse = new ReclamationReponse();
        reponse.setMessage(reponseDTO.getMessage());
        reponse.setReponsePar(reponseDTO.getReponsePar());

        ReclamationReponse saved = reclamationService.addReponseToReclamation(id, reponse);
        return ResponseEntity.ok(convertToReponseDTO(saved));
    }

    @GetMapping("/{id}/reponses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<List<ReclamationReponseDTO>> getResponses(@PathVariable Long id) {
        List<ReclamationReponse> reponses = reclamationService.getReponsesByReclamation(id);
        List<ReclamationReponseDTO> dtos = reponses.stream()
                .map(this::convertToReponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<ReclamationDTO> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, StatutReclamation> request) {
        StatutReclamation statut = request.get("statut");
        Reclamation updated = reclamationService.changeStatus(id, statut);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("NEW_TODAY", reclamationService.countNewToday());
        stats.put("UNREAD", reclamationService.countUnread());
        stats.put("RESOLUE", reclamationService.countByStatut(StatutReclamation.RESOLUE));
        stats.put("EN_ATTENTE", reclamationService.countByStatut(StatutReclamation.EN_ATTENTE));
        stats.put("EN_COURS", reclamationService.countByStatut(StatutReclamation.EN_COURS));
        stats.put("REJETEE", reclamationService.countByStatut(StatutReclamation.REJETEE));
        stats.put("TOTAL", reclamationService.countTotal());

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/reponses/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        reclamationService.deleteResponse(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reponses/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<ReclamationReponseDTO> updateResponse(
            @PathVariable Long id,
            @RequestBody ReclamationReponseDTO reponseDTO) {
        ReclamationReponse reponse = new ReclamationReponse();
        reponse.setMessage(reponseDTO.getMessage());

        ReclamationReponse updated = reclamationService.updateResponse(id, reponse);
        return ResponseEntity.ok(convertToReponseDTO(updated));
    }

//    @PatchMapping("/{id}/mark-as-read")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
//    public ResponseEntity<ReclamationDTO> markAsRead(@PathVariable Long id) {
//        Reclamation reclamation = reclamationService.getReclamationById(id);
//        reclamation.setLu(true);
//        Reclamation updated = reclamationService.updateReclamation(id, reclamation);
//        return ResponseEntity.ok(convertToDTO(updated));
//    }
@PatchMapping("/{id}/mark-as-read")
@Transactional // Add this annotation
public ResponseEntity<ReclamationDTO> markAsRead(@PathVariable Long id) {
    Reclamation reclamation = reclamationService.getReclamationById(id);
    reclamation.setLu(true);
    Reclamation updated = reclamationService.updateReclamation(id, reclamation);
    return ResponseEntity.ok(convertToDTO(updated));
}
    @GetMapping("/by-agence/{codeAgence}")
    @PreAuthorize("hasRole('CHEF_AGENCE') and @userSecurityService.hasAccessToAgence(#codeAgence, authentication)")
    public ResponseEntity<Page<ReclamationDTO>> getByAgence(
            @PathVariable String codeAgence,
            Pageable pageable,
            @RequestParam(required = false) StatutReclamation statut,
            @RequestParam(required = false) Long placementId,
            @RequestParam(required = false) String search) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByAgence(
                codeAgence, pageable, statut, placementId, search);

        Page<ReclamationDTO> dtoPage = reclamations.map(this::convertToDTO);
        return ResponseEntity.ok(dtoPage);
    }

    private ReclamationDTO convertToDTO(Reclamation reclamation) {
        ReclamationDTO dto = new ReclamationDTO();
        dto.setId(reclamation.getId());
        if (reclamation.getPlacement() != null) {
            dto.setPlacementId(reclamation.getPlacement().getId());
        }
        dto.setTitre(reclamation.getTitre());
        dto.setTypeProbleme(reclamation.getTypeProbleme());
        dto.setLu(reclamation.getLu());
        dto.setDescription(reclamation.getDescription());
        dto.setStatut(reclamation.getStatut());
        dto.setDateCreation(reclamation.getDateCreation());

        if (reclamation.getReponses() != null) {
            dto.setReponses(reclamation.getReponses().stream()
                    .map(this::convertToReponseDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private ReclamationReponseDTO convertToReponseDTO(ReclamationReponse reponse) {
        ReclamationReponseDTO dto = new ReclamationReponseDTO();
        dto.setId(reponse.getId());
        if (reponse.getReclamation() != null) {
            dto.setReclamationId(reponse.getReclamation().getId());
        }
        dto.setDateReponse(reponse.getDateReponse());
        dto.setMessage(reponse.getMessage());
        dto.setReponsePar(reponse.getReponsePar());
        return dto;
    }
}