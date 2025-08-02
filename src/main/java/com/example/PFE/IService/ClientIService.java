package com.example.PFE.IService;

import java.util.List;
import java.util.Optional;

import com.example.PFE.Model.Client;
import com.example.PFE.Model.Placement;

public interface ClientIService {
	Client saveClient(Client client);
	 boolean isLoyalClient(Long clientId);
	    boolean hasUnpaidCredit(Long clientId);
	    Optional<Client> getClientById(Long id);

		// Ajout des nouvelles méthodes
	    List<Client> getAllClientsWithPlacements();
	    Optional<Client> getClientWithPlacements(Long id);

		List<Client> getAllClients();

	void deleteClient(Long id);
	Client updateClient(Long id, Client client); // Nouvelle méthode
	 List<Client> getClientsByAgence(String codeAgence);
	List<Client> getClientsByAgenceCode(String codeAgence);
	List<Client> getClientsByAgenceWithPlacements(String codeAgence);
}
