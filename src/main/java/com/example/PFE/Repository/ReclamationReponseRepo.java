package com.example.PFE.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PFE.Model.Reclamation;
import com.example.PFE.Model.ReclamationReponse;

public interface ReclamationReponseRepo extends JpaRepository<ReclamationReponse, Long>  {
	  List<ReclamationReponse> findByReclamationId(Long reclamationId);
}
