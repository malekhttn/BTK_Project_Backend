package com.example.PFE.ServiceImpl;

import com.example.PFE.IService.UserIService;
import com.example.PFE.Model.User;
import com.example.PFE.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserIService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User updateUser(User user) {
        // Don't encode password if it hasn't changed
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().startsWith("$2a$")) { // Check if already encoded
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        // Handle temp password separately
        if (user.getTempPassword() != null && !user.getTempPassword().isEmpty()) {
            if (!user.getTempPassword().startsWith("$2a$")) {
                user.setTempPassword(passwordEncoder.encode(user.getTempPassword()));
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }


    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return findByUsername(username);
    }

    @Override
    public User updateProfile(User userUpdate) {
        User currentUser = getCurrentUser();

        // Mise à jour des champs autorisés
        if (userUpdate.getUsername() != null && !userUpdate.getUsername().isEmpty()) {
            currentUser.setUsername(userUpdate.getUsername());
        }

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isEmpty()) {
            if (userRepository.existsByEmailAndIdNot(userUpdate.getEmail(), currentUser.getId())) {
                throw new RuntimeException("Email already in use");
            }
            currentUser.setEmail(userUpdate.getEmail());
        }

        return userRepository.save(currentUser);
    }



    @Override
    public User changePassword(String currentPassword, String newPassword) {
        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(currentUser);
    }

    // UserServiceImpl.java
    @Override
    public User toggleBlockUser(Long id) {
        User user = getUserById(id);
        user.setBlock(user.getBlock() == 0 ? 1 : 0); // Basculer entre 0 et 1
        return userRepository.save(user);
    }


}