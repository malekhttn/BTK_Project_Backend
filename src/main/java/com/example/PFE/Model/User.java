package com.example.PFE.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "code_agence")
    private String codeAgence;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resetTokenExpiry;
    @Column(name = "temp_password")
    private String tempPassword;

    @Column(name = "temp_password_expiry")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tempPasswordExpiry;
    public enum Role {
        ADMIN,
        SUPER_ADMIN,
        CHEF_AGENCE
    }
}