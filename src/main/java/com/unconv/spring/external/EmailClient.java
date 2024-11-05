package com.unconv.spring.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailClient {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean emailEnabled;

    @Autowired
    public EmailClient(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress,
            @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;

        // Check if email configuration is complete
        this.emailEnabled = StringUtils.hasText(fromAddress) && StringUtils.hasText(mailHost);
    }

    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            // No-op if email configuration is incomplete
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromAddress);

        mailSender.send(message);
    }
}
