package com.prudhvi.swacch.utils;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.service.NotificationService;

@Component
public class PasswordReminderScheduler {

    private final UserRepo userRepo;
    private final NotificationService notificationService;

    public PasswordReminderScheduler(UserRepo userRepo,
                                     NotificationService notificationService) {
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    // Run every day at 9 AM (example)
    //@Scheduled(cron = "0 */5 * * * *")
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPasswordChangeReminders() {
        List<User> collectors = userRepo.findByRoleAndPasswordChangedFalse(UserRole.COLLECTOR);

        for (User c : collectors) {
            notificationService.sendCollectorCredentialsReminder(
                    c.getEmail(),
                    c.getName()
            );
        }
    }
}
