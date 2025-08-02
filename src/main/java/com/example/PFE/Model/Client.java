package com.example.PFE.Model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;



@Data
@Entity
@Table(name = "client")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "placements.client"}) // Add this

public class Client {
  	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;



	private String name;
	private boolean hasUnpaidCredit; // Ensure this field is included
	private LocalDate registrationDate;
	private String codeAgence; // Added agency code

	@Column(name = "is_loyal", nullable = false) // Explicit column name
	private boolean loyal = false; // Renamed field to follow Java conventions
	//@ToString.Exclude
	//@JsonIgnore
	// @JsonManagedReference
	// Relation One-to-Many avec Placement
/*	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<Placement> placements;*/

	// Option 1: Bidirectional relationship (recommended)
//	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JsonIgnore // This will exclude placements from normal serialization
//	private List<Placement> placements;
// Prevent the placement list from being serialized
//	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
//
//	private List<Placement> placements;


	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnoreProperties({"client"}) // Prevent serialization loop
	private List<Placement> placements;
	public boolean shouldBeLoyal() {
		if (registrationDate == null) return false;
		return LocalDate.now().isAfter(registrationDate.plusMonths(6));
	}

	// Add getter and setter
	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

	// Update getter to consider both manual and automatic loyalty


	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public boolean hasUnpaidCredit() {  // Preferred for 'has' prefix
		return hasUnpaidCredit;
	}

	public void setHasUnpaidCredit(boolean hasUnpaidCredit) {
		this.hasUnpaidCredit = hasUnpaidCredit;
	}


	// Getters et setters personnalisés
	public boolean isLoyal() {
		return this.loyal;
	}

	public void setLoyal(boolean loyal) {
		this.loyal = loyal;
	}



	public boolean shouldBeAutoLoyal() {
		if (registrationDate == null) return false;
		return LocalDate.now().isAfter(registrationDate.plusMonths(6));
	}

	public boolean isActuallyLoyal() {
		return isLoyal() || shouldBeAutoLoyal() || (placements != null && placements.size() > 3);
	}
}
