package com.prudhvi.swacch.utils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.service.NotificationService;

@Component
public class PasswordReminderScheduler {

    private final UserRepo userRepo;
    private final NotificationService notificationService;
    
    public static final Logger log= LoggerFactory.getLogger(PasswordReminderScheduler.class);

    public PasswordReminderScheduler(UserRepo userRepo,
                                     NotificationService notificationService) {
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    // Run every day at 9 AM (example)
    //@Scheduled(cron = "0 */5 * * * *")
    @Scheduled(cron = "0 0 21 * * *")
    public void sendPasswordChangeReminders() {
        List<User> collectors = userRepo.findByRoleAndPasswordChangedFalse(UserRole.COLLECTOR);
        if(CollectionUtils.isEmpty(collectors)) {
        	return;
        }
        ExecutorService ex=Executors.newFixedThreadPool(5);
        long start=System.currentTimeMillis();
        List<?> list = collectors.stream()
        		.map(e->ex.submit(()->notificationService.sendCollectorCredentials(e.getEmail(), e.getName())))
        		.toList();
        int failures=0;
        for(Object f:list) {
        	try {
                ((Future<?>) f).get(30, TimeUnit.SECONDS); // surfaces exceptions, bounds wait time
            } catch (ExecutionException e) {
                failures++;
                log.error("Failed to send credentials email", e.getCause());
            } catch (TimeoutException e) {
                failures++;
                log.error("Timed out sending credentials email", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while sending emails", e);
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info("{} ms total time to send mails to {} collectors ({} failures)",
                (end - start), collectors.size(), failures);
        
        ex.shutdown();
    }
}
