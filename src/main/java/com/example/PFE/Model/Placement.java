package com.example.PFE.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "placement")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "client.placements"}) // Add this
public class Placement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codeAgence;
    private String typeClient;
    private String pin;
    private String naturePlacement;
    private Double montant;
    private Double tauxPropose;
    private Integer duree;
    private String origineFonds;
    private Boolean engagementRelation;
    private Double tauxCredit;
    private Boolean nantissement;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private PlacementStatus statut = PlacementStatus.EN_ATTENTE;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"placements"})
    private Client client;

    @OneToMany(mappedBy = "placement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"placement", "reponses"})
    private List<Reclamation> reclamations;

    public Long getClientId() {
        return client != null ? client.getId() : null;
    }

    public void setClientId(Long clientId) {
        if (client == null) {
            client = new Client();
        }
        client.setId(clientId);
    }

    public void accepter() {
        this.statut = PlacementStatus.ACCEPTE;
    }

    public void refuser() {
        this.statut = PlacementStatus.REFUSE;
    }

    public boolean estEnAttente() {
        return PlacementStatus.EN_ATTENTE.equals(this.statut);
    }
}