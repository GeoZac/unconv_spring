package com.unconv.spring.external;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailClientTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks private EmailClient emailClient;

    @Value("${spring.mail.username}")
    private String mFromAddress;

    @Value("${spring.mail.host}")
    private String mMailHost;

    @Mock private MimeMessage mimeMessage;

    @Test
    void testSendEmailWhenEmailEnabled() {
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "This is a test email.";
        String fromAddress = "sender@example.com";
        String mailHost = "smtp.example.com";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromAddress);

        emailClient = new EmailClient(mailSender, fromAddress, mailHost);
        emailClient.sendEmail(to, subject, text);

        verify(mailSender, times(1)).send(message);
    }

    @Test
    void testSendEmailWithHTMLContentWhenEmailEnabled() {
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String htmlContent = "<h1>This is a test email.</h1>";
        String fromAddress = "sender@example.com";
        String mailHost = "smtp.example.com";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailClient = new EmailClient(mailSender, fromAddress, mailHost);
        emailClient.sendEmailWithHTMLContent(to, subject, htmlContent);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmailWhenEmailNotEnabled() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "This email should not be sent.";
        String fromAddress = ""; // Invalid fromAddress
        String mailHost = ""; // Invalid mailHost

        emailClient = new EmailClient(mailSender, fromAddress, mailHost);
        emailClient.sendEmail(to, subject, text);

        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEmailClientShouldNotSendEmailIfNoRecipient() {
        String to = ""; // Empty recipient
        String subject = "Test Subject";
        String text = "This email should not be sent because there's no recipient.";
        String fromAddress = "sender@example.com";
        String mailHost = "smtp.example.com";

        emailClient = new EmailClient(mailSender, fromAddress, mailHost);
        emailClient.sendEmail(to, subject, text);

        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailWhenNoSubject() {
        String to = "recipient@example.com";
        String subject = ""; // Empty subject
        String text = "This email should be sent without a subject.";
        String fromAddress = "sender@example.com";
        String mailHost = "smtp.example.com";

        emailClient = new EmailClient(mailSender, fromAddress, mailHost);
        emailClient.sendEmail(to, subject, text);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromAddress);

        verify(mailSender, times(1)).send(message);
    }

    @Test
    void testSendEmailWithInvalidConfiguration() {
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Email shouldn't be sent because configuration is incomplete.";

        emailClient = new EmailClient(mailSender, "", "");

        emailClient.sendEmail(to, subject, text);

        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }
}
