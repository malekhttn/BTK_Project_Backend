package com.example.PFE.Controller;

import com.example.PFE.Config.CustomUserDetails;
import com.example.PFE.Config.JwtUtil;
import com.example.PFE.Config.UserDetailsServiceImpl;
import com.example.PFE.Model.User;
import com.example.PFE.Model.UserCredentials;
import com.example.PFE.ServiceImpl.EmailService;
import com.example.PFE.IService.UserIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserIService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserIService userService,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder , UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;

    }
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (user.getRole() == User.Role.CHEF_AGENCE && (user.getCodeAgence() == null || user.getCodeAgence().isEmpty())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.saveUser(user));
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody User user) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            user.getUsername(),
//                            user.getPassword()
//                    )
//            );
//
//            User authenticatedUser = userService.findByUsername(user.getUsername());
//
//            // Create claims with user information
//            Map<String, Object> claims = new HashMap<>();
//            claims.put("role", authenticatedUser.getRole().name());
//            claims.put("codeAgence", authenticatedUser.getCodeAgence());
//
//            final String jwt = jwtUtil.generateToken(claims, user.getUsername());
//
//            return ResponseEntity.ok(new AuthResponse(jwt, authenticatedUser.getRole().name(), authenticatedUser.getCodeAgence()));
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
//        }
//    }
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody UserCredentials credentials) {
    try {
        User user = userService.findByUsername(credentials.getUsername());
        // Vérifier si l'utilisateur est bloqué
        if (user.getBlock() == 1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Votre compte est bloqué");
        }
        // Check if using temporary password
        if (user.getTempPassword() != null) {
            if (!passwordEncoder.matches(credentials.getPassword(), user.getTempPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid temporary password");
            }
            if (user.getTempPasswordExpiry().before(new Date())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Temporary password expired");
            }

            // Authenticate with temp password
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // Normal authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getUsername(),
                            credentials.getPassword()
                    )
            );
        }

        // Generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("codeAgence", user.getCodeAgence());
        String jwt = jwtUtil.generateToken(claims, user.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt, user.getRole().name(),
                user.getCodeAgence()));
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
    }
}



    @PostMapping("/auto-reset-password")
    public ResponseEntity<?> autoResetPassword(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "User not found"));
            }

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "User has no email address configured"));
            }

            String tempPassword = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(tempPassword);

            user.setTempPassword(encodedPassword);
            user.setTempPasswordExpiry(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
            userService.updateUser(user);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), username, tempPassword);
                return ResponseEntity.ok()
                        .body(Collections.singletonMap("message", "Password reset email sent successfully"));
            } catch (Exception e) {  // Changed from EmailSendingException to Exception
                logger.error("Failed to send email for user {}: {}", username, e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Collections.singletonMap("message", "Temporary email service outage. Please try again later."));
            }
        } catch (Exception e) {
            logger.error("Password reset failed for user: {}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("message", "Error processing request: " + e.getMessage()));
        }
    }
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            // Check if user is authenticated with temp password
            boolean isTempPasswordAuth = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("TEMP_PASSWORD_AUTH"));

            if (!isTempPasswordAuth &&
                    !passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Current password is incorrect");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setTempPassword(null);
            user.setTempPasswordExpiry(null);
            userService.updateUser(user);

            return ResponseEntity.ok().body("Password changed successfully");

        } catch (Exception e) {
            logger.error("Password change failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password");
        }
    }

    @PostMapping("/auto-login")
    public ResponseEntity<?> autoLogin(@RequestParam String username,
                                       @RequestParam String password) {
        try {
            // 1. Find user
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("Auto-login failed - User not found: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid credentials"));
            }

            // 2. Verify temp password exists and matches
            if (user.getTempPassword() == null) {
                logger.error("No temp password found for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "No temporary password set"));
            }

            if (!passwordEncoder.matches(password, user.getTempPassword())) {
                logger.error("Temp password mismatch for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid credentials"));
            }

            // 3. Check expiry
            if (user.getTempPasswordExpiry() == null ||
                    user.getTempPasswordExpiry().before(new Date())) {
                logger.error("Temp password expired for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Temporary password expired"));
            }

            // 4. Authenticate - Create authentication token manually since it's temp password
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. Generate token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().name());
            claims.put("codeAgence", user.getCodeAgence());
            String jwt = jwtUtil.generateToken(claims, username);

            // 6. Invalidate temp password
            user.setTempPassword(null);
            user.setTempPasswordExpiry(null);
            userService.updateUser(user);

            return ResponseEntity.ok(new AuthResponse(jwt, user.getRole().name(),
                    user.getCodeAgence()));
        } catch (Exception e) {
            logger.error("Auto-login failed for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Auto-login failed"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        try {
            if (jwtUtil.canRefresh(refreshToken)) {
                String username = jwtUtil.extractUsername(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                String newToken = jwtUtil.refreshToken(refreshToken, userDetails);

                return ResponseEntity.ok(new AuthResponse(newToken,
                        ((CustomUserDetails)userDetails).getRole().name(),
                        ((CustomUserDetails)userDetails).getCodeAgence()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed");
        }
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static class AuthResponse {
        private final String token;
        private final String role;
        private final String codeAgence;

        public AuthResponse(String token, String role, String codeAgence) {
            this.token = token;
            this.role = role;
            this.codeAgence = codeAgence;
        }

        // Getters
        public String getToken() { return token; }
        public String getRole() { return role; }
        public String getCodeAgence() { return codeAgence; }
    }
}