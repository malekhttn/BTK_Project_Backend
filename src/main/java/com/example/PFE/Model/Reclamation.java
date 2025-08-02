package com.example.PFE.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reclamation")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "placement_id")
//    @JsonBackReference
//    private Placement placement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_id", nullable = false)
    @JsonIgnoreProperties({"reclamations", "reponses"})
    private Placement placement;

    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_probleme")
    private ProblemePlacement typeProbleme;

    @Column(name = "lu")
    private Boolean lu = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutReclamation statut = StatutReclamation.EN_ATTENTE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation")
    private Date dateCreation;

//    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL)
//    private List<ReclamationReponse> reponses;
@OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnoreProperties("reclamation") // This prevents circular reference
private List<ReclamationReponse> reponses;
    @PrePersist
    protected void onCreate() {
        this.dateCreation = new Date();
        if (this.titre == null && this.typeProbleme != null) {
            this.titre = this.typeProbleme.toString();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Placement getPlacement() { return placement; }
    public void setPlacement(Placement placement) { this.placement = placement; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public ProblemePlacement getTypeProbleme() { return typeProbleme; }
    public void setTypeProbleme(ProblemePlacement typeProbleme) { this.typeProbleme = typeProbleme; }

    public Boolean getLu() { return lu; }
    public void setLu(Boolean lu) { this.lu = lu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StatutReclamation getStatut() { return statut; }
    public void setStatut(StatutReclamation statut) { this.statut = statut; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public List<ReclamationReponse> getReponses() { return reponses; }
    public void setReponses(List<ReclamationReponse> reponses) { this.reponses = reponses; }
}