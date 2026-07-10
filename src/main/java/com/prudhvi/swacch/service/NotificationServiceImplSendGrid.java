package com.prudhvi.swacch.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
@Primary
public class NotificationServiceImplSendGrid implements NotificationService{
	
	@Value("${sendgrid.api-key}")
    private String sendgridApiKey;

	@Override
	public void sendCollectorCredentials(String email, String name, String tempPassword) {
        Email from = new Email("prudhvipotnuru11@gmail.com");
        String subject = "Your Swacch collector account details";
        Email to = new Email(email);
        Content content;
        if(tempPassword == null) {
        content = new Content("text/plain",
        		"Hi " + name + ",\n\n" +
                        "This is a reminder to change your temporary password and set a new one.\n" +
                        "If you already changed it, you can ignore this email.\n\n" +
                        "Regards,\nSwacch Admin");
        } else {
        	content = new Content("text/plain",
                    "Hi " + name + ",\n\n" +
                    "Your temporary password is: " + tempPassword + "\n" +
                    "Please login and change your password.\n");
        }
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }


	@Override
	public void sendCollectorCredentials(String toEmail, String name) {
		sendCollectorCredentials(toEmail, name, null);
	}

}
