package com.example.PFE.Config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityService {

    public boolean hasAccessToAgence(String codeAgence, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return codeAgence.equals(userDetails.getCodeAgence());
    }
}