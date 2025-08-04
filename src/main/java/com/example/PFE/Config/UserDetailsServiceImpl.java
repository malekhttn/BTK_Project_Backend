package com.example.PFE.Config;

import com.example.PFE.Model.User;
import com.example.PFE.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username)
        );

        return new CustomUserDetails(user);
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<User> userOptional = userRepository.findByUsername(username);
//
//        User user = userOptional.orElseThrow(() ->
//                new UsernameNotFoundException("User not found with username: " + username)
//        );
//
//        List<GrantedAuthority> authorities = Collections.singletonList(
//                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
//        );
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                authorities
//        );
//    }

//    @Bean
//    public UserDetailsService userDetailsService(UserRepo userRepository) {
//        return username -> {
//            // Trouver votre User entity
//            User userEntity = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
//
//            if (userEntity.isBlocked()) {
//                throw new DisabledException("Ce compte utilisateur est bloqué");
//            }
//
//            // Créer un UserDetails (Spring Security) à partir de votre User entity
//            return new org.springframework.security.core.userdetails.User(
//                    userEntity.getUsername(),
//                    userEntity.getPassword(),
//                    getAuthorities(userEntity));
//        };
//    }
//
//    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
//        // Convertir le rôle de l'utilisateur en GrantedAuthority
//        // Note: Assurez-vous que user.getRole() retourne une valeur non nulle
//        String role = "ROLE_" + user.getRole().name();
//        return Collections.singletonList(new SimpleGrantedAuthority(role));
//    }

}
