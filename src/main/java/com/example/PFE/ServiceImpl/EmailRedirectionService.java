package com.example.PFE.ServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailRedirectionService {

    @Value("${email.redirection.enabled}")
    private boolean redirectionEnabled;

    @Value("${email.redirection.address}")
    private String redirectionAddress;

    public String getFinalRecipient(String originalRecipient) {
        return redirectionEnabled ? redirectionAddress : originalRecipient;
    }
}