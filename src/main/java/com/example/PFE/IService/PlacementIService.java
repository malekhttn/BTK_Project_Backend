package com.example.PFE.IService;

import com.example.PFE.Model.Placement;
import java.util.List;

public interface PlacementIService {
    Placement savePlacement(Placement placement);
    List<Placement> getAllPlacements();
    Placement getPlacementById(Long id);
    Placement updatePlacement(Placement placement);
    void deletePlacement(Long id);
     List<Placement> getPlacementsByAgence(String codeAgence);
}