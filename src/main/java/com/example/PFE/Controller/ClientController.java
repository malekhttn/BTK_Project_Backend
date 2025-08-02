package com.example.PFE.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.PFE.DTO.ClientDTO;
import com.example.PFE.DTO.PlacementDTO;
import com.example.PFE.Exception.ResourceNotFoundException;
import com.example.PFE.Model.User;
import com.example.PFE.Repository.ClientRepo;
import com.example.PFE.ServiceImpl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.PFE.IService.ClientIService;
import com.example.PFE.Model.Client;
import com.example.PFE.Model.Placement;
import com.example.PFE.ServiceImpl.ClientServiceImpl;


@RestController
@RequestMapping("/clients")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")

public class ClientController {


    @Autowired
    private ClientServiceImpl clientService;
    @Autowired
    private ClientRepo clientRepository;
    @Autowired
    private UserServiceImpl userService;
    @GetMapping
    public List<ClientDTO> getAllClients() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() == User.Role.CHEF_AGENCE) {
            return clientService.getClientsByAgence(currentUser.getCodeAgence()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return clientService.getAllClients().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    @GetMapping("/by-agence/{codeAgence}")
    @PreAuthorize("hasRole('CHEF_AGENCE') and @userSecurityService.hasAccessToAgence(#codeAgence, authentication)")
    public ResponseEntity<List<ClientDTO>> getClientsByAgence(@PathVariable String codeAgence) {
        // Utiliser une méthode qui charge les clients avec leurs placements en une seule requête
        List<Client> clients = clientService.getClientsByAgenceWithPlacements(codeAgence);
        List<ClientDTO> dtos = clients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

//    @PostMapping
//    public ResponseEntity<ClientDTO> createClient(@RequestBody Client client) {
//        Client savedClient = clientService.saveClient(client);
//        return ResponseEntity.ok(convertToDTO(savedClient));
//    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody Client client) {
        // Initialisation explicite des valeurs
        client.setLoyal(client.isLoyal()); // Force la valeur
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.ok(convertToDTO(savedClient));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody Client client) {
        try {
            Client updatedClient = clientService.updateClient(id, client);
            return ResponseEntity.ok(convertToDTO(updatedClient));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{clientId}/hasUnpaidCredit")
    public boolean hasUnpaidCredit(@PathVariable Long clientId) {
        return clientService.hasUnpaidCredit(clientId);
    }

    @GetMapping("/{clientId}/isLoyal")
    public boolean isLoyalClient(@PathVariable Long clientId) {
        return clientService.isLoyalClient(clientId);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
//        Optional<Client> client = clientService.getClientById(id);
//        return client.map(value -> ResponseEntity.ok(convertToDTO(value)))
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        Optional<Client> client = clientService.getClientById(id);

        if (client.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (currentUser.getRole() == User.Role.CHEF_AGENCE &&
                !client.get().getPlacements().stream()
                        .anyMatch(p -> p.getCodeAgence().equals(currentUser.getCodeAgence()))) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(convertToDTO(client.get()));
    }


    @GetMapping("/ALL")
    public List<ClientDTO> getAllClientsWithoutPlacementsField() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/ALLWithoutPlacement")
    public List<ClientDTO> getClientsWithoutPlacement() {
        List<Client> clients = clientRepository.findAllWithoutPlacements();
        return clients.stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/ALLWithPlacement")
    public List<ClientDTO> getClientsWithPlacement() {
        List<Client> clients = clientRepository.findAllWithPlacements();
        return clients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/with-placements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public List<ClientDTO> getAllClientsWithPlacements() {
        List<Client> clients = clientService.getAllClientsWithPlacements();
        return clients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/with-placements")
    public ResponseEntity<ClientDTO> getClientWithPlacements(@PathVariable Long id) {
        Optional<Client> client = clientService.getClientWithPlacements(id);
        return client.map(value -> ResponseEntity.ok(convertToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


//    private ClientDTO convertToDTO(Client client) {
//        ClientDTO dto = new ClientDTO();
//        dto.setId(client.getId());
//        dto.setName(client.getName());
//        dto.setHasUnpaidCredit(client.hasUnpaidCredit());
//        dto.setLoyal(client.isLoyal());
//        dto.setRegistrationDate(client.getRegistrationDate());
//
//        if (client.getPlacements() != null) {
//            dto.setPlacements(client.getPlacements().stream()
//                    .map(this::convertToPlacementDTO)
//                    .collect(Collectors.toList()));
//        }
//
//        return dto;
//    }


    private ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO(
                client.getId(),
                client.getName(),
                client.hasUnpaidCredit(),
                client.isLoyal(),
                client.getRegistrationDate(),
                client.getCodeAgence()
        );

        // Vérifier si les placements sont initialisés (éviter LazyInitializationException)
        try {
            if (client.getPlacements() != null && !Hibernate.isInitialized(client.getPlacements())) {
                // Si la collection n'est pas initialisée, ne pas essayer d'y accéder
                dto.setPlacements(Collections.emptyList());
            } else if (client.getPlacements() != null) {
                dto.setPlacements(client.getPlacements().stream()
                        .map(this::convertToPlacementDTO)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            // En cas d'erreur, initialiser une liste vide
            dto.setPlacements(Collections.emptyList());
        }

        return dto;
    }

    private ClientDTO convertToSimpleDTO(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getName(),
                client.hasUnpaidCredit(),
                client.isLoyal(),
                client.getRegistrationDate(),
                client.getCodeAgence()
        );
    }

    private PlacementDTO convertToPlacementDTO(Placement placement) {
        PlacementDTO dto = new PlacementDTO();
        dto.setId(placement.getId());
        dto.setCodeAgence(placement.getCodeAgence());
        dto.setTypeClient(placement.getTypeClient());
        dto.setPin(placement.getPin());
        dto.setNaturePlacement(placement.getNaturePlacement());
        dto.setMontant(placement.getMontant());
        dto.setTauxPropose(placement.getTauxPropose());
        dto.setDuree(placement.getDuree());
        dto.setOrigineFonds(placement.getOrigineFonds());
        dto.setEngagementRelation(placement.getEngagementRelation());
        dto.setTauxCredit(placement.getTauxCredit());
        dto.setNantissement(placement.getNantissement());
        dto.setStatut(placement.getStatut());
        dto.setMessage(placement.getMessage());
        return dto;
    }


    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long id) throws WriterException, IOException {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        String qrData = "CLIENT:" + client.getId();
        int width = 300;
        int height = 300;

        BitMatrix matrix = new QRCodeWriter().encode(
                qrData,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(out.toByteArray());
    }

    @GetMapping("/by-qrcode")
    public ResponseEntity<Client> getClientByQrCode(@RequestParam String data) {
        if (!data.startsWith("CLIENT:")) {
            return ResponseEntity.badRequest().build();
        }

        Long clientId = Long.parseLong(data.substring(7));
        return clientService.getClientWithPlacements(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/qrcode-data")
    public ResponseEntity<String> getClientQRCodeData(@PathVariable Long id) {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode clientData = mapper.createObjectNode();

        try {
            clientData.put("id", client.getId());
            clientData.put("name", client.getName());
            clientData.put("isLoyal", client.isLoyal());
            clientData.put("hasUnpaidCredit", client.hasUnpaidCredit());
            clientData.put("registrationDate", client.getRegistrationDate().toString());

            ArrayNode placements = mapper.createArrayNode();
            if (client.getPlacements() != null) {
                for (Placement placement : client.getPlacements()) {
                    ObjectNode placementNode = mapper.createObjectNode();
                    placementNode.put("id", placement.getId());
                    placementNode.put("nature", placement.getNaturePlacement());
                    placementNode.put("amount", placement.getMontant());
                    placementNode.put("duration", placement.getDuree());
                    placementNode.put("rate", placement.getTauxPropose());
                    placements.add(placementNode);
                }
            }
            clientData.set("placements", placements);

            return ResponseEntity.ok(clientData.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error generating QR data\"}");
        }
    }
}
