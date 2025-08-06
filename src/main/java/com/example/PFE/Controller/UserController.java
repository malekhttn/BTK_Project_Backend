package com.example.PFE.Controller;

import com.example.PFE.DTO.UserDTO;
import com.example.PFE.IService.UserIService;
import com.example.PFE.Model.User;
import com.example.PFE.ServiceImpl.ReclamationServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {


    @Autowired
    private UserIService userService;
    private static final Logger logger = LogManager.getLogger(ReclamationServiceImpl.class);

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        // Assurer que le block est à 0 pour les nouveaux utilisateurs
        user.setBlock(0);

        // Validation supplémentaire pour CHEF_AGENCE
        if (user.getRole() == User.Role.CHEF_AGENCE && (user.getCodeAgence() == null || user.getCodeAgence().isEmpty())) {
            return ResponseEntity.badRequest().body(null);
        }

        User created = userService.saveUser(user);
        return ResponseEntity.ok(convertToDTO(created));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> dtos = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(convertToDTO(user));
    }

    // ... other methods remain the same ...

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setBlock(user.getBlock());
        return dto;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }


    // Endpoint pour l'utilisateur courant
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // Endpoint pour mettre à jour le profil (utilise User comme modèle)
    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<User> updateProfile(@RequestBody User userUpdate) {
        return ResponseEntity.ok(userService.updateProfile(userUpdate));
    }

    // Endpoint pour changer le mot de passe
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CHEF_AGENCE')")
    public ResponseEntity<User> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.changePassword(currentPassword, newPassword));
    }
    // UserController.java
    @PostMapping("/{id}/toggle-block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> toggleBlockUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Empêcher de bloquer soi-même
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName().equals(user.getUsername())) {
            return ResponseEntity.badRequest().body(null);
        }

        User updated = userService.toggleBlockUser(id);
        return ResponseEntity.ok(convertToDTO(updated));
    }

}