package com.unconv.spring.tasks;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Scheduled component that checks for sensor authentication tokens nearing their expiry and sends
 * reminder emails to the associated users.
 *
 * <p>This class is scheduled to run once every 7 days (604800000 milliseconds). If a token is found
 * to expire within one month from the current date, a reminder email is sent to the associated user
 * using a Thymeleaf template.
 *
 * <p>The email contains information about the sensor system, the username, and the token's expiry
 * date.
 *
 * @see SensorAuthTokenService
 * @see EmailClient
 * @see SpringTemplateEngine
 */
@Component
@EnableScheduling
public class SensorAuthTokenExpiryReminder {

    @Autowired private SensorAuthTokenService sensorAuthTokenService;

    @Autowired private EmailClient emailClient;

    @Autowired SpringTemplateEngine templateEngine;

    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm 'UTC'", Locale.ENGLISH);

    /**
     * Scheduled method that runs every 7 days to remind users of expiring sensor auth tokens.
     *
     * <p>This method retrieves all sensor authentication tokens, checks if any are expiring within
     * one month, and sends a reminder email to the user associated with each expiring token.
     *
     * <p>The email uses a Thymeleaf template named {@code sensor-auth-token-expiry-reminder.html}
     * and includes the username, sensor name, and formatted expiry date.
     */
    @Scheduled(fixedRate = 604800000, initialDelay = 86400000)
    public void remindSensorAuthTokenExpiry() {
        int page = 0;
        int size = 10;
        Page<SensorAuthToken> tokenPage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            tokenPage = sensorAuthTokenService.findSensorAuthTokens(pageable);

            tokenPage.getContent().stream()
                    .filter(this::isExpiringWithinOneMonth)
                    .forEach(this::sendReminderEmail);

            page++;
        } while (!tokenPage.isLast());
    }

    private boolean isExpiringWithinOneMonth(SensorAuthToken token) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = token.getExpiry();
        return Math.abs(ChronoUnit.MONTHS.between(expiry, now)) < 1;
    }

    private void sendReminderEmail(SensorAuthToken token) {
        UnconvUser user = token.getSensorSystem().getUnconvUser();
        String email = user.getEmail();
        String subject = "⚠️ Sensor Auth Token Expiry Reminder";

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("sensorName", token.getSensorSystem().getSensorName());
        context.setVariable("expiryDate", token.getExpiry().format(EXPIRY_FORMATTER));

        String body = templateEngine.process("sensor-auth-token-expiry-reminder.html", context);
        emailClient.sendEmailWithHTMLContent(email, subject, body);
    }
}
