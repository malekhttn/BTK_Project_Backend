package com.example.PFE.Config;

import com.example.PFE.Model.Client;
import com.example.PFE.Repository.ClientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;

public class ScheduledTasks {
    private ClientRepo clientRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateClientLoyaltyStatus() {
        List<Client> clients = clientRepository.findAll();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        clients.forEach(client -> {
            if (client.getRegistrationDate() != null &&
                    client.getRegistrationDate().isBefore(sixMonthsAgo) &&
                    !client.isLoyal()) {
                client.setLoyal(true);
                clientRepository.save(client);
            }
        });
    }
}
