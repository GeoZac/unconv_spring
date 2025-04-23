package com.unconv.spring.external;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service class responsible for sending emails. It utilizes the {@link JavaMailSender} to send
 * simple email messages. This client is enabled only if the email configuration properties are
 * provided.
 */
@Service
public class EmailClient {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean emailEnabled;

    /**
     * Constructs an {@code EmailClient} with the provided {@code JavaMailSender} and email
     * properties.
     *
     * @param mailSender the {@link JavaMailSender} used to send emails
     * @param fromAddress the email address used as the sender's address
     * @param mailHost the host of the email server; used to verify if email configuration is
     *     complete
     */
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

    /**
     * Sends an email with the specified recipient, subject, and text.
     *
     * <p>The email will only be sent if the email configuration is enabled. If the recipient's
     * address is blank or email configuration is incomplete, this method performs no action.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param text the body text of the email
     */
    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            // No-op if email configuration is incomplete
            return;
        }

        if (to.isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromAddress);

        mailSender.send(message);
    }

    public void sendEmailWithHTMLContent(String to, String subject, String htmlContent) {
        if (!emailEnabled || to.isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromAddress);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
