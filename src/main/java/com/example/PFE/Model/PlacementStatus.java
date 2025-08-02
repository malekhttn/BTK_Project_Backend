package com.example.PFE.Model;

public enum PlacementStatus {
    EN_ATTENTE("En attente"),  // Exactement comme stocké en base
    ACCEPTE("Accepte"),        // Sans accent
    REFUSE("Refuse");          // Sans accent

    private final String displayValue;

    PlacementStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static PlacementStatus fromString(String text) {
        for (PlacementStatus status : PlacementStatus.values()) {
            if (status.name().equalsIgnoreCase(text) || 
                status.displayValue.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}