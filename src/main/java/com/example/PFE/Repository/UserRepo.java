package com.example.PFE.Repository;

import com.example.PFE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);
    boolean existsByEmail(String email);
    User  findByResetToken(String token);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<User> findByUsername(String username);

}