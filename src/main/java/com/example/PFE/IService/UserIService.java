package com.example.PFE.IService;

import com.example.PFE.Model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserIService  {
    User getCurrentUser();
    User updateProfile(User userUpdate);
    User changePassword(String currentPassword, String newPassword);
    User saveUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
    User updateUser(User user);
    void deleteUser(Long id);

    User findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String token);
    User toggleBlockUser(Long id); // Une seule méthode pour basculer entre bloqué/débloqué
}