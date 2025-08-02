package com.example.PFE.ServiceImpl;

import com.example.PFE.Config.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import java.util.Base64;
@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY = 30; // seconds
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int SOCKET_TIMEOUT = 10000; // 10 seconds

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final JavaMailSender mailSender;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final String fromName;
    private final EmailRedirectionService emailRedirectionService;

    private final JwtUtil jwtutil;
    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.properties.mail.smtp.from:#{null}}") String fromName , EmailRedirectionService emailRedirectionService,
            JwtUtil jwtutil) {
        this.mailSender = mailSender;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = username; // Using SMTP username as from email
        this.fromName = fromName != null ? fromName : "BTK Bank"; // Default name if not specified

        this.jwtutil = jwtutil;
        this.emailRedirectionService = emailRedirectionService;

    }

//
//    @Value("${spring.mail.username}")
//    private String fromEmail;

    public void sendPasswordResetEmail(String originalTo, String username, String tempPassword)
            throws MessagingException {
        try {
            String finalTo = emailRedirectionService.getFinalRecipient(originalTo);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // Gestion sécurisée de l'encodage pour l'expéditeur
            try {
                helper.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail); // Fallback sans encodage
            }

            helper.setTo(finalTo);
            helper.setSubject("[Password Reset] Original recipient: " + originalTo);

            String content = buildPasswordResetContent(username, tempPassword, originalTo);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}", originalTo, e);
            throw e;
        }
    }


    public void sendReclamationResponse(String originalTo, String responseContent)
            throws MessagingException {
        try {
            String finalTo = emailRedirectionService.getFinalRecipient(originalTo);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // Gestion sécurisée de l'encodage pour l'expéditeur
            try {
                helper.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail); // Fallback sans encodage
            }

            helper.setTo(finalTo);
            helper.setSubject("[Reclamation] Original recipient: " + originalTo);

            String content = buildReclamationContent(responseContent, originalTo);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send reclamation response to {}", originalTo, e);
            throw e;
        }
    }

//    private String buildPasswordResetContent(String username, String tempPassword, String originalRecipient) {
//        return String.format("""
//            <html>
//            <body>
//                <p><strong>NOTE:</strong> This email would normally be sent to: %s</p>
//                <h2>Password Reset Request</h2>
//                <p>Username: %s</p>
//                <p>Temporary Password: %s</p>
//            </body>
//            </html>
//            """, originalRecipient, username, tempPassword);
//    }
private String buildPasswordResetContent(String username, String tempPassword, String originalRecipient) {
    String imageHtml = "";

    try {
        // Chemin absolu depuis les ressources (adapté à votre structure de projet)
        String imagePath = "/static/images/logo-btk.png";
        InputStream inputStream = getClass().getResourceAsStream(imagePath);

        if (inputStream != null) {
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String imageMimeType = "image/png"; // Adaptez si votre image est en JPG

            imageHtml = String.format("<img src=\"data:%s;base64,%s\" alt=\"BTK Bank Logo\" style=\"max-height: 50px;\">",
                    imageMimeType, base64Image);
        } else {
            logger.warn("Image not found in resources: {}", imagePath);
            // Fallback vers une URL externe
            imageHtml = "<img src=\"main\\resources\\static\\images\" alt=\"BTK Bank Logo\" style=\"max-height: 50px;\">";
        }
    } catch (IOException e) {
        logger.error("Error loading image for email template", e);
        // Fallback vers une URL externe
        imageHtml = "<img src=\"main\\resources\\static\\images\" alt=\"BTK Bank Logo\" style=\"max-height: 50px;\">";
    }

    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: #333333;
                    margin: 0;
                    padding: 0;
                    background-color: #f7f7f7;
                }
                .email-container {
                    max-width: 600px;
                    margin: 20px auto;
                    background: #ffffff;
                    border-radius: 8px;
                    overflow: hidden;
                    box-shadow: 0 0 10px rgba(0,0,0,0.1);
                }
                .header {
                    background-color: #0056b3;
                    padding: 20px;
                    text-align: center;
                }
                .content {
                    padding: 30px;
                }
                .footer {
                    background-color: #f5f5f5;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #666666;
                }
                .button {
                    display: inline-block;
                    padding: 12px 24px;
                    background-color: #0056b3;
                    color: #ffffff !important;
                    text-decoration: none;
                    border-radius: 4px;
                    font-weight: bold;
                    margin: 20px 0;
                }
                .info-box {
                    background-color: #f8f9fa;
                    border-left: 4px solid #0056b3;
                    padding: 15px;
                    margin: 20px 0;
                }
                .note {
                    font-size: 14px;
                    color: #666666;
                    border: 1px dashed #cccccc;
                    padding: 10px;
                    margin-bottom: 20px;
                    background-color: #fff8e1;
                }
                @media only screen and (max-width: 600px) {
                    .content {
                        padding: 20px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    %s
                </div>
                
                <div class="content">
                    <div class="note">
                        <strong>NOTE :</strong> Cet email serait normalement envoyé à : %s
                    </div>
                    
                    <h2 style="color: #0056b3; margin-top: 0;">Demande de réinitialisation de mot de passe</h2>
                    
                    <p>Bonjour <strong>%s</strong>,</p>
                    
                    <p>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte BTK Bank.</p>
                    
                    <div class="info-box">
                        <p><strong>Identifiant :</strong> %s</p>
                        <p><strong>Mot de passe temporaire :</strong> %s</p>
                        <p><small>(Ce mot de passe expirera dans 1 heure)</small></p>
                    </div>
                    
                    <p>Pour des raisons de sécurité, nous vous recommandons de :</p>
                    <ol>
                        <li>Vous connecter immédiatement avec ce mot de passe temporaire</li>
                        <li>Changer pour un nouveau mot de passe sécurisé</li>
                        <li>Ne jamais partager votre mot de passe</li>
                    </ol>
                    
                    <p style="text-align: center;">
                        <a href="http://localhost:4200/reset-password" class="button">Réinitialiser mon mot de passe</a>
                    </p>
                    
                    <p>Si vous n'êtes pas à l'origine de cette demande, veuillez contacter immédiatement notre service client.</p>
                    
                    <p>Cordialement,<br>L'équipe BTK Bank</p>
                </div>
                
                <div class="footer">
                    <p>© 2023 BTK Bank. Tous droits réservés.</p>
                    <p>Ceci est un message automatique - merci de ne pas y répondre directement.</p>
                </div>
            </div>
        </body>
        </html>
        """,
            imageHtml,             // %s 1 - Logo (base64 ou URL de fallback)
            originalRecipient,     // %s 2 - Destinataire original
            username,              // %s 3 - Nom d'utilisateur (dans le message)
            username,              // %s 4 - Nom d'utilisateur (dans l'info-box)
            tempPassword);         // %s 5 - Mot de passe temporaire
}
    private String buildReclamationContent(String response, String originalRecipient) {
        return String.format("""
            <html>
            <body>
                <p><strong>NOTE:</strong> This reclamation response would normally be sent to: %s</p>
                <h2>Reclamation Response</h2>
                <p>%s</p>
            </body>
            </html>
            """, originalRecipient, response);
    }

      private MimeMessage createResetEmail(String toEmail, String username, String tempPassword)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress(fromEmail, fromName));
        helper.setTo(toEmail);
        helper.setSubject("Réinitialisation de votre mot de passe BTK Bank");

        String resetLink = "http://localhost:4200/reset-password?token="
                + URLEncoder.encode(generateResetToken(username), StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);

        helper.setText(buildPasswordResetEmail(resetLink, username, tempPassword), true);

        // Important headers for email deliverability
        message.addHeader("X-Mailer", "BTK-Mail-Sender");
        message.addHeader("X-Priority", "1");
        message.addHeader("Importance", "high");

        return message;
    }
    private String generateResetToken(String username) {
        // Génère un token JWT valide 1 heure
        return jwtutil.generateToken(Collections.singletonMap("reset", "true"), username);
    }

    private String buildPasswordResetEmail(String resetLink, String username, String tempPassword) {
        return String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                <h2 style="color: #0056b3;">Réinitialisation de mot de passe</h2>
                <p>Bonjour %s,</p>
                <p>Vous avez demandé à réinitialiser votre mot de passe pour votre compte BTK Bank.</p>
                
                <div style="background: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>Mot de passe temporaire:</strong> %s</p>
                    <p style="color: red;">Ce mot de passe expirera dans 1 heure.</p>
                </div>
                
                <p>Cliquez sur le lien suivant pour définir un nouveau mot de passe :</p>
                <p style="text-align: center; margin: 20px 0;">
                    <a href="%s" style="background-color: #0056b3; color: white; padding: 10px 20px; 
                    text-decoration: none; border-radius: 5px;">Réinitialiser mon mot de passe</a>
                </p>
                
                <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>
                <p style="margin-top: 30px; color: #666; font-size: 0.9em;">
                    Cordialement,<br>L'équipe BTK Bank
                </p>
            </div>
        </body>
        </html>
        """, username, tempPassword, resetLink);
    }


    private String buildResetPasswordEmail(String resetLink) {
        return "<html>"
                + "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>"
                + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>"
                + "<h2 style='color: #0056b3;'>Réinitialisation de votre mot de passe</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Vous avez demandé à réinitialiser votre mot de passe pour votre compte Banque BTK.</p>"
                + "<p>Cliquez sur le lien suivant pour procéder à la réinitialisation :</p>"
                + "<p style='text-align: center; margin: 20px 0;'>"
                + "<a href='" + resetLink + "' style='background-color: #0056b3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Réinitialiser mon mot de passe</a>"
                + "</p>"
                + "<p>Ce lien expirera dans 1 heure.</p>"
                + "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>"
                + "<p>Cordialement,<br>L'équipe Banque BTK</p>"
                + "</div>"
                + "</body>"
                + "</html>";
    }


//    @Autowired
//    public EmailService(
//            JavaMailSender mailSender,
//            @Value("${spring.mail.host}") String host,
//            @Value("${spring.mail.port}") int port,
//            @Value("${spring.mail.username}") String username,
//            @Value("${spring.mail.password}") String password) {
//        this.mailSender = mailSender;
//        this.host = host;
//        this.port = port;
//        this.username = username;
//        this.password = password;
//    }

    @PostConstruct
    public void init() {
        if (testSmtpConnection()) {
            startConnectionMonitor();
        } else {
            logger.warn("Initial SMTP connection test failed. Will retry in monitor.");
            startConnectionMonitor();
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down EmailService scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void sendEmail(String to, String subject, String text) {
        sendEmail(to, subject, text, false);
    }



    public void sendEmail(String to, String subject, String text, boolean isHtml) {
        validateEmailParameters(to, subject, text);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(fromEmail, fromName));
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text, isHtml);

                // Headers pour améliorer la délivrabilité
                message.addHeader("X-Mailer", "BTK-Mail-Sender");
                message.addHeader("X-Priority", "1");
                message.addHeader("Importance", "high");

                mailSender.send(message);
                logger.info("Email sent successfully to: {}", to);
                return;
            } catch (Exception ex) {
                logger.error("Email send attempt {} failed for {}: {}", attempt, to, ex.getMessage());

                if (attempt == MAX_RETRIES) {
                    throw new EmailSendingException("Failed to send email after " + MAX_RETRIES + " attempts", ex);
                }

                try {
                    Thread.sleep(INITIAL_RETRY_DELAY * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new EmailSendingException("Email send retry interrupted", ie);
                }
            }
        }
    }

    private int calculateBackoffDelay(int attempt) {
        return INITIAL_RETRY_DELAY * attempt; // Exponential backoff would be better
    }

    private void validateEmailParameters(String to, String subject, String text) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email address cannot be empty");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject cannot be empty");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Email content cannot be empty");
        }
    }


    public boolean testSmtpConnection() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.connectiontimeout", CONNECTION_TIMEOUT);
            props.put("mail.smtp.timeout", SOCKET_TIMEOUT);

            // Ajoutez explicitement le mot de passe
            props.put("mail.smtp.password", password);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);
            transport.close();
            return true;
        } catch (Exception e) {
            logger.error("SMTP connection test failed to {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }



    private void startConnectionMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!testSmtpConnection()) {
                    logger.warn("SMTP connection is down. Will retry in next cycle.");
                }
            } catch (Exception e) {
                logger.error("Unexpected error in connection monitor", e);
            }
        }, 1, 5, TimeUnit.MINUTES); // Initial delay 1 min, then every 5 minutes
    }

    class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }}
}