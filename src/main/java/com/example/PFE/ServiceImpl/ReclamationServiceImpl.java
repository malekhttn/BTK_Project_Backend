package com.example.PFE.ServiceImpl;

import com.example.PFE.IService.ReclamationIService;
import com.example.PFE.Model.*;
import com.example.PFE.Repository.ReclamationRepo;
import com.example.PFE.Repository.ReclamationReponseRepo;
import jakarta.persistence.EntityGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ReclamationServiceImpl implements ReclamationIService {
    private static final Logger logger = LogManager.getLogger(ReclamationServiceImpl.class);

    private final ReclamationRepo reclamationRepository;
    private final ReclamationReponseRepo reponseRepository;
    private final EmailService emailService;




    @Override
    public Page<Reclamation> getReclamationsByAgence(
            String codeAgence,
            Pageable pageable,
            StatutReclamation statut,
            Long placementId,
            String search) {

        return reclamationRepository.findAllByPlacementCodeAgence(
                codeAgence, statut, placementId, search, pageable);
    }

    @Autowired
    public ReclamationServiceImpl(ReclamationRepo reclamationRepository,
                                  ReclamationReponseRepo reponseRepository,
                                  EmailService emailService) {
        this.reclamationRepository = reclamationRepository;
        this.reponseRepository = reponseRepository;
        this.emailService = emailService;
    }

    @Override
    public Reclamation createReclamation(Reclamation reclamation) {
        reclamation.setDateCreation(new Date());
        reclamation.setLu(false);
        return reclamationRepository.save(reclamation);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getAllReclamations(Pageable pageable, StatutReclamation statut, Long placementId, String search) {
        return reclamationRepository.findAllWithReponsesAndPlacement(statut, placementId, search, pageable);
    }
//    @Override
//    @Transactional(readOnly = true)
//    public Page<Reclamation> getAllReclamations(Pageable pageable, StatutReclamation statut, Long placementId, String search) {
//        Specification<Reclamation> spec = Specification.where(null);
//
//        if (statut != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("statut"), statut));
//        }
//        if (placementId != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("placement").get("id"), placementId));
//        }
//        if (search != null && !search.isEmpty()) {
//            spec = spec.and((root, query, cb) ->
//                    cb.like(root.get("titre"), "%" + search + "%"));
//        }
//
//        return reclamationRepository.findAll(spec, pageable);
//    }


    @Override
    @Transactional(readOnly = true)
    public Reclamation getReclamationById(Long id) {
        return reclamationRepository.findByIdWithReponses(id)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + id));
    }

    @Override
    public Reclamation updateReclamation(Long id, Reclamation reclamation) {
        Reclamation existing = getReclamationById(id);
        existing.setTitre(reclamation.getTitre());
        existing.setPlacement(reclamation.getPlacement());
        existing.setTypeProbleme(reclamation.getTypeProbleme());
        existing.setDescription(reclamation.getDescription());
        existing.setStatut(reclamation.getStatut());
        existing.setLu(reclamation.getLu());
        return reclamationRepository.save(existing);
    }

    @Override
    public void deleteReclamation(Long id) {
        reclamationRepository.deleteById(id);
    }
    @Override
    public ReclamationReponse addReponseToReclamation(Long reclamationId, ReclamationReponse reponse) {
        Reclamation reclamation = getReclamationById(reclamationId);
        reponse.setReclamation(reclamation);
        reponse.setDateReponse(new Date());

        if (reclamation.getStatut() == StatutReclamation.EN_ATTENTE) {
            reclamation.setStatut(StatutReclamation.EN_COURS);
            reclamationRepository.save(reclamation);
        }

        ReclamationReponse savedResponse = reponseRepository.save(reponse);

        sendEmailNotification(reclamation, reponse);

        return savedResponse;
    }

    private void sendEmailNotification(Reclamation reclamation, ReclamationReponse reponse) {
        try {
            String recipientEmail = getRecipientEmailForReclamation(reclamation);
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                logger.warn("No recipient email found for reclamation {}", reclamation.getId());
                return;
            }

            String subject = String.format("[BTK] New Response - Reclamation #%d", reclamation.getId());
            String content = buildEmailContent(reclamation, reponse);

            if (!emailService.testSmtpConnection()) {
                logger.error("SMTP connection failed. Email not sent.");
                return;
            }

            emailService.sendEmail(recipientEmail, subject, content, true);
            logger.info("Email notification sent successfully for reclamation {}", reclamation.getId());

        } catch (Exception e) {
            logger.error("Failed to send email notification for reclamation {}: {}", reclamation.getId(), e.getMessage());
        }
    }

    private String getRecipientEmailForReclamation(Reclamation reclamation) {
        // Implement logic to get recipient email (admin or related user)
        // For now, using a default email
        return "malek.chtioui127@gmail.com";
    }

//    private String buildEmailContent(Reclamation reclamation, ReclamationReponse reponse) {
//        return String.format("""
//            <html>
//            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
//                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
//                    <h2 style="color: #0056b3;">New Response - Reclamation #%d</h2>
//                    <h3>%s</h3>
//                    <div style="background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
//                        <p><strong>Type:</strong> %s</p>
//                        <p><strong>Status:</strong> %s</p>
//                        <p><strong>Description:</strong> %s</p>
//                    </div>
//                    <h4>New Response:</h4>
//                    <div style="background: #f0f8ff; padding: 15px; border-radius: 5px;">
//                        <p><strong>From:</strong> %s</p>
//                        <p><strong>Message:</strong> %s</p>
//                        <p><strong>Date:</strong> %s</p>
//                    </div>
//                    <p style="margin-top: 20px; font-size: 0.9em; color: #666;">
//                        This is an automated email. Please do not reply.
//                    </p>
//                </div>
//            </body>
//            </html>
//            """,
//                reclamation.getId(),
//                reclamation.getTitre(),
//                reclamation.getTypeProbleme(),
//                reclamation.getStatut(),
//                reclamation.getDescription(),
//                reponse.getReponsePar(),
//                reponse.getMessage(),
//                reponse.getDateReponse()
//        );
//    }
private String buildEmailContent(Reclamation reclamation, ReclamationReponse reponse) {
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
                    color: white;
                }
                .content {
                    padding: 30px;
                }
                .footer {
                    background-color: #f5f5f5;
                    padding: 15px;
                    text-align: center;
                    font-size: 12px;
                    color: #666666;
                }
                .reclamation-info {
                    background-color: #f8f9fa;
                    border-left: 4px solid #0056b3;
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 4px;
                }
                .response-info {
                    background-color: #e8f4fd;
                    border-left: 4px solid #28a745;
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 4px;
                }
                .info-label {
                    font-weight: bold;
                    color: #0056b3;
                    display: inline-block;
                    width: 100px;
                }
                .divider {
                    border-top: 1px solid #eeeeee;
                    margin: 20px 0;
                }
                @media only screen and (max-width: 600px) {
                    .content {
                        padding: 15px;
                    }
                    .info-label {
                        display: block;
                        width: auto;
                        margin-bottom: 5px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h2>Réponse à votre réclamation #%d</h2>
                </div>
                
                <div class="content">
                    <h3 style="color: #0056b3; margin-top: 0;">%s</h3>
                    
                    <div class="reclamation-info">
                        <p><span class="info-label">Type:</span> %s</p>
                        <p><span class="info-label">Statut:</span> <strong>%s</strong></p>
                        <p><span class="info-label">Description:</span> %s</p>
                    </div>
                    
                    <div class="divider"></div>
                    
                    <h4 style="color: #28a745;">Notre réponse :</h4>
                    
                    <div class="response-info">
                        <p><span class="info-label">De:</span> %s</p>
                        <p><span class="info-label">Date:</span> %s</p>
                        <div style="margin-top: 10px; padding: 10px; background-color: white; border-radius: 4px;">
                            %s
                        </div>
                    </div>
                    
                    <div style="margin-top: 30px; text-align: center;">
                        <p>Vous pouvez suivre l'évolution de votre réclamation sur votre espace client.</p>
                    </div>
                </div>
                
                <div class="footer">
                    <p>© 2023 BTK Bank. Tous droits réservés.</p>
                    <p>Ceci est un message automatique - merci de ne pas y répondre directement.</p>
                </div>
            </div>
        </body>
        </html>
        """,
            reclamation.getId(),
            reclamation.getTitre(),
            reclamation.getTypeProbleme(),
            reclamation.getStatut(),
            reclamation.getDescription(),
            reponse.getReponsePar(),
            reponse.getDateReponse(),
            reponse.getMessage()
    );
}


    // ... rest of the methods remain unchanged ...

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationReponse> getReponsesByReclamation(Long reclamationId) {
        return reponseRepository.findByReclamationId(reclamationId);
    }

    @Override
    public Reclamation changeStatus(Long id, StatutReclamation nouveauStatut) {
        Reclamation reclamation = getReclamationById(id);
        reclamation.setStatut(nouveauStatut);
        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationStats getReclamationStats() {
        ReclamationStats stats = new ReclamationStats();
        Date now = new Date();
        Date fiveMinutesAgo = new Date(now.getTime() - (5 * 60 * 1000));

        stats.setNEW_TODAY(reclamationRepository.countByLuFalseAndDateCreationAfter(fiveMinutesAgo));
        stats.setUNREAD(reclamationRepository.countByLuFalseAndDateCreationBefore(fiveMinutesAgo));
        stats.setEN_ATTENTE(reclamationRepository.countByStatut(StatutReclamation.EN_ATTENTE));
        stats.setEN_COURS(reclamationRepository.countByStatut(StatutReclamation.EN_COURS));
        stats.setRESOLUE(reclamationRepository.countByStatut(StatutReclamation.RESOLUE));
        stats.setREJETEE(reclamationRepository.countByStatut(StatutReclamation.REJETEE));
        stats.setTOTAL(reclamationRepository.count()); // Ajoutez cette ligne

        return stats;
    }

    @Override
    public void deleteResponse(Long id) {
        reponseRepository.deleteById(id);
    }

    @Override
    public ReclamationReponse updateResponse(Long id, ReclamationReponse reponse) {
        ReclamationReponse existing = reponseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));
        existing.setMessage(reponse.getMessage());
        return reponseRepository.save(existing);
    }


    @Override
    public long countNewToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        return reclamationRepository.countByLuFalseAndDateCreationAfter(fiveMinutesAgo);
    }

    @Override
    public long countUnread() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        return reclamationRepository.countByLuFalseAndDateCreationBefore(fiveMinutesAgo);
    }

    @Override
    public long countByStatut(StatutReclamation statut) {
        return reclamationRepository.countByStatut(statut);
    }

    @Override
    public long countTotal() {
        return reclamationRepository.count();
    }
}