package com.example.PFE.ServiceImpl;

import com.example.PFE.IService.ClientIService;
import com.example.PFE.IService.PlacementIService;
import com.example.PFE.Model.Placement;
import com.example.PFE.Model.Client;
import com.example.PFE.Repository.PlacementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlacementServiceImpl implements PlacementIService {
    @Autowired
    private PlacementRepo placementRepository;

    @Autowired
    private ClientIService clientService;

    @Override
    public Placement savePlacement(Placement placement) {
        if (placement.getClient() == null || placement.getClient().getId() == null) {
            throw new IllegalArgumentException("Client must be provided for a placement.");
        }

        Client client = clientService.getClientById(placement.getClient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + placement.getClient().getId()));

        placement.setClient(client);

        Double tauxPropose = calculateProposedRate(placement);
        placement.setTauxPropose(tauxPropose);

        return placementRepository.save(placement);
    }

    private Double calculateProposedRate(Placement placement) {
        Client client = placement.getClient();
        Double baseRate = 2.5;

        if (client != null && client.isLoyal()) {
            baseRate -= 0.5;
        }

        if (client != null && client.hasUnpaidCredit()) {
            baseRate += 1.0;
        }

        switch (placement.getNaturePlacement()) {
            case "CAT":
                baseRate += 0.3;
                break;
            case "BDC":
                baseRate += 0.2;
                break;
            case "CD":
                baseRate += 0.1;
                break;
        }

        if (placement.getMontant() > 100000) {
            baseRate -= 0.2;
        }

        if (placement.getDuree() > 12) {
            baseRate += 0.5;
        }

        return Math.round(baseRate * 100.0) / 100.0;
    }

    @Override
    public List<Placement> getPlacementsByAgence(String codeAgence) {
        return placementRepository.findByCodeAgence(codeAgence);
    }

    @Override
    public List<Placement> getAllPlacements() {
        return placementRepository.findAllWithClient(); // Doit inclure le client
    }

    @Override
    public Placement getPlacementById(Long id) {
        return placementRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Placement updatePlacement(Placement placement) {
        // Verify placement exists
        Placement existingPlacement = placementRepository.findById(placement.getId())
                .orElseThrow(() -> new IllegalArgumentException("Placement not found with id: " + placement.getId()));

        // Update only allowed fields
        existingPlacement.setCodeAgence(placement.getCodeAgence());
        existingPlacement.setTypeClient(placement.getTypeClient());
        existingPlacement.setPin(placement.getPin());
        existingPlacement.setNaturePlacement(placement.getNaturePlacement());
        existingPlacement.setMontant(placement.getMontant());
        existingPlacement.setTauxPropose(placement.getTauxPropose());
        existingPlacement.setDuree(placement.getDuree());
        existingPlacement.setOrigineFonds(placement.getOrigineFonds());
        existingPlacement.setEngagementRelation(placement.getEngagementRelation());
        existingPlacement.setTauxCredit(placement.getTauxCredit());
        existingPlacement.setNantissement(placement.getNantissement());
        existingPlacement.setStatut(placement.getStatut());
        existingPlacement.setMessage(placement.getMessage());

        // Handle client update if needed
        if (placement.getClient() != null && placement.getClient().getId() != null) {
            Client client = clientService.getClientById(placement.getClient().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Client not found"));
            existingPlacement.setClient(client);
        }

        return placementRepository.save(existingPlacement);
    }
    @Override
    public void deletePlacement(Long id) {
        placementRepository.deleteById(id);
    }
}