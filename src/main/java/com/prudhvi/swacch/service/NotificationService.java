package com.prudhvi.swacch.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCollectorCredentials(String toEmail, String name, String tempPassword) {
    	if(StringUtils.isBlank(toEmail)) {
    		return;
    	}
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Your Swacch Collector Login");
        msg.setText("Hi " + name + ",\n\n" +
                "Your temporary password is: " + tempPassword + "\n" +
                "Please log in and change it after first login.\n\n" +
                "Regards,\nSwacch Admin");
        mailSender.send(msg);
    }
    
    public void sendCollectorCredentialsReminder(String toEmail, String name) {
    	if(StringUtils.isBlank(toEmail)) {
    		return;
    	}
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Reminder: Please change your Swacch password");
        msg.setText("Hi " + name + ",\n\n" +
                "This is a reminder to change your temporary password and set a new one.\n" +
                "If you already changed it, you can ignore this email.\n\n" +
                "Regards,\nSwacch Admin");
        mailSender.send(msg);
    }
}
