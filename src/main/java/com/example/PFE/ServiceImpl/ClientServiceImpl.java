package com.example.PFE.ServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.PFE.Exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PFE.IService.ClientIService;
import com.example.PFE.Model.Client;
import com.example.PFE.Repository.ClientRepo;

@Service
@Transactional
public class ClientServiceImpl implements ClientIService {

	@Autowired
	private ClientRepo clientRepository;

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<Client> getClientsByAgence(String codeAgence) {
		return clientRepository.findByPlacementsCodeAgence(codeAgence);
	}
	@Override
	public boolean hasUnpaidCredit(Long clientId) {
		Client client = clientRepository.findById(clientId).orElse(null);
		return client != null && client.hasUnpaidCredit();
	}

	@Override
	public boolean isLoyalClient(Long clientId) {
		Client client = clientRepository.findById(clientId).orElse(null);
		return client != null && (client.isLoyal() || client.shouldBeLoyal());
	}

	@Override
	public Optional<Client> getClientById(Long id) {
		return clientRepository.findById(id);
	}

	@Override
	public Client saveClient(Client client) {
		if (client.getId() == null) {
			client.setRegistrationDate(LocalDate.now());
		}
		return clientRepository.save(client);
	}

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<Client> getAllClientsWithPlacements() {
		return clientRepository.findAllWithPlacements();
	}

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public Optional<Client> getClientWithPlacements(Long id) {
		return clientRepository.findByIdWithPlacements(id);
	}

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<Client> getAllClients() {
		return clientRepository.findAll();
	}

	@Override
	public void deleteClient(Long id) {
		clientRepository.deleteById(id);
	}

	@Override
	public Client updateClient(Long id, Client client) {
		if (id == null) {
			throw new IllegalArgumentException("Client ID cannot be null");
		}

		Client existingClient = clientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

		if (client.getName() != null) {
			existingClient.setName(client.getName());
		}

		existingClient.setHasUnpaidCredit(client.hasUnpaidCredit());
		existingClient.setLoyal(client.isLoyal());
		existingClient.setCodeAgence(client.getCodeAgence());

		return clientRepository.save(existingClient);
	}
	@Override
	public List<Client> getClientsByAgenceCode(String codeAgence) {
		return clientRepository.findByCodeAgence(codeAgence);
	}

	@Override
	public List<Client> getClientsByAgenceWithPlacements(String codeAgence) {
		return clientRepository.findByCodeAgenceWithPlacements(codeAgence);
	}
	// ClientService.java

}