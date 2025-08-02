package com.example.PFE.Model;

public enum ProblemePlacement {
    RETARD_PAIEMENT("Retard de paiement constaté"),
    ERREUR_MONTANT("Différence de montant constatée"),
    PROBLEME_ECHEANCE("Problème avec la date d'échéance"),
    TAUX_INCORRECT("Taux d'intérêt incorrect"),
    DOCUMENT_MANQUANT("Document contractuel manquant"),
    PRELEVEMENT_NON_AUTHORISE("Prélèvement non autorisé"),
    FRAIS_INATTENDUS("Frais ou commissions inattendus"),
    NON_RESPECT_DELAI("Non-respect des délais de traitement"),
    INFORMATION_INCORRECTE("Information incorrecte fournie"),
    PROBLEME_RACHAT("Problème lors du rachat anticipé"),
    AUTRE_PROBLEME("Autre problème non listé");

    private final String titre;

    ProblemePlacement(String titre) {
        this.titre = titre;
    }

    public String getTitre() {
        return titre;
    }
}