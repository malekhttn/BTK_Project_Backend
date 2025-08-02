package com.example.PFE.Controller;

import com.example.PFE.Config.CustomUserDetails;
import com.example.PFE.DTO.PlacementDTO;
import com.example.PFE.Model.Client;
import com.example.PFE.Model.Placement;
import com.example.PFE.Model.PlacementStatus;
import com.example.PFE.Model.User;
import com.example.PFE.Repository.PlacementRepo;
import com.example.PFE.ServiceImpl.ExcelExportService;
import com.example.PFE.ServiceImpl.PlacementServiceImpl;
import com.example.PFE.ServiceImpl.UserServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/placements")
@CrossOrigin
public class PlacementController {
    @Autowired
    private PlacementServiceImpl placementService;
    @Autowired
    private PlacementRepo placementRepository;
    @Autowired
    private ExcelExportService excelExportService;
    @Autowired
    private UserServiceImpl userService;


//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
//    public ResponseEntity<Placement> createPlacement(@RequestBody Placement placement) {
//        Placement savedPlacement = placementService.savePlacement(placement);
//        return ResponseEntity.ok(savedPlacement);
//    }
@PostMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
public ResponseEntity<PlacementDTO> createPlacement(@RequestBody Placement placement) {
    Placement savedPlacement = placementService.savePlacement(placement);
    return ResponseEntity.ok(convertToDTO(savedPlacement));
}

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    @Transactional
    public ResponseEntity<PlacementDTO> updatePlacement(
            @PathVariable Long id,
            @RequestBody PlacementDTO placementDTO) {

        // Convert DTO to entity
        Placement placement = convertToEntity(placementDTO);
        placement.setId(id);

        Placement updatedPlacement = placementService.updatePlacement(placement);
        return ResponseEntity.ok(convertToDTO(updatedPlacement));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public List<PlacementDTO> getAllPlacements(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<Placement> placements;
        if (user.getRole() == User.Role.CHEF_AGENCE) {
            placements = placementService.getPlacementsByAgence(user.getCodeAgence());
        } else {
            placements = placementService.getAllPlacements();
        }

        return placements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-agence/{codeAgence}")
    @PreAuthorize("hasRole('CHEF_AGENCE') and @userSecurityService.hasAccessToAgence(#codeAgence, authentication)")
    public List<PlacementDTO> getPlacementsByAgence(@PathVariable String codeAgence) {
        List<Placement> placements = placementService.getPlacementsByAgence(codeAgence);
        return placements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public PlacementDTO getPlacementById(@PathVariable Long id) {
        Placement placement = placementService.getPlacementById(id);
        return convertToDTO(placement);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public void deletePlacement(@PathVariable Long id) {
        placementService.deletePlacement(id);
    }

//    @PatchMapping("/{id}/accept")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
//    public ResponseEntity<Placement> acceptPlacement(@PathVariable Long id) {
//        Placement placement = placementService.getPlacementById(id);
//        placement.setStatut(PlacementStatus.ACCEPTE);
//        Placement updatedPlacement = placementService.updatePlacement(placement);
//        return ResponseEntity.ok(updatedPlacement);
//    }
@PatchMapping("/{id}/accept")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
public ResponseEntity<PlacementDTO> acceptPlacement(@PathVariable Long id) {
    Placement placement = placementService.getPlacementById(id);
    placement.setStatut(PlacementStatus.ACCEPTE);
    Placement updatedPlacement = placementService.updatePlacement(placement);
    return ResponseEntity.ok(convertToDTO(updatedPlacement));
}

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<PlacementDTO> rejectPlacement(@PathVariable Long id) {
        Placement placement = placementService.getPlacementById(id);
        placement.setStatut(PlacementStatus.REFUSE);
        Placement updatedPlacement = placementService.updatePlacement(placement);
        return ResponseEntity.ok(convertToDTO(updatedPlacement));
    }
    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<InputStreamResource> exportPlacementsToExcel(Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<Placement> placements;

        if (userDetails.getRole() == User.Role.CHEF_AGENCE) {
            placements = placementService.getPlacementsByAgence(userDetails.getCodeAgence());
        } else {
            placements = placementService.getAllPlacements();
        }

        ByteArrayInputStream in = excelExportService.placementsToExcel(placements);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=placements.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    private Placement convertToEntity(PlacementDTO dto) {
        Placement placement = new Placement();
        placement.setId(dto.getId());
        placement.setCodeAgence(dto.getCodeAgence());
        placement.setTypeClient(dto.getTypeClient());
        placement.setPin(dto.getPin());
        placement.setNaturePlacement(dto.getNaturePlacement());
        placement.setMontant(dto.getMontant());
        placement.setTauxPropose(dto.getTauxPropose());
        placement.setDuree(dto.getDuree());
        placement.setOrigineFonds(dto.getOrigineFonds());
        placement.setEngagementRelation(dto.getEngagementRelation());
        placement.setTauxCredit(dto.getTauxCredit());
        placement.setNantissement(dto.getNantissement());
        placement.setStatut(dto.getStatut());
        placement.setMessage(dto.getMessage());

        if (dto.getClientId() != null) {
            Client client = new Client();
            client.setId(dto.getClientId());
            placement.setClient(client);
        }

        return placement;
    }
    private PlacementDTO convertToDTO(Placement placement) {
        PlacementDTO dto = new PlacementDTO();
        dto.setId(placement.getId());
        dto.setCodeAgence(placement.getCodeAgence());
        dto.setTypeClient(placement.getTypeClient());
        dto.setPin(placement.getPin());
        dto.setNaturePlacement(placement.getNaturePlacement());
        dto.setMontant(placement.getMontant());
        dto.setTauxPropose(placement.getTauxPropose());
        dto.setDuree(placement.getDuree());
        dto.setOrigineFonds(placement.getOrigineFonds());
        dto.setEngagementRelation(placement.getEngagementRelation());
        dto.setTauxCredit(placement.getTauxCredit());
        dto.setNantissement(placement.getNantissement());
        dto.setStatut(placement.getStatut());
        dto.setMessage(placement.getMessage());

        if (placement.getClient() != null) {
            dto.setClientId(placement.getClient().getId());
            dto.setClientName(placement.getClient().getName()); // Make sure this is set
        }

        return dto;
    }
}