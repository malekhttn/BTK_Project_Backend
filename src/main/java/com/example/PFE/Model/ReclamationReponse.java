package com.example.PFE.Model;

import jakarta.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reclamation_reponse")
public class ReclamationReponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_reponse")
    private Date dateReponse;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "reponse_par")
    private String reponsePar = "Système";

    @PrePersist
    protected void onCreate() {
        if (this.dateReponse == null) {
            this.dateReponse = new Date();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reclamation getReclamation() { return reclamation; }
    public void setReclamation(Reclamation reclamation) { this.reclamation = reclamation; }

    public Date getDateReponse() { return dateReponse; }
    public void setDateReponse(Date dateReponse) { this.dateReponse = dateReponse; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReponsePar() { return reponsePar; }
    public void setReponsePar(String reponsePar) { this.reponsePar = reponsePar; }
}