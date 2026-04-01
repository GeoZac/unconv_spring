package com.unconv.spring.tasks;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
 * Scheduled component that checks for sensor authentication tokens and sends appropriate emails.
 *
 * <p>This class manages two types of token notifications:
 *
 * <ul>
 *   <li><strong>Expiring Tokens:</strong> Tokens that will expire within one month but have not yet
 *       expired. Reminder emails are sent to encourage renewal.
 *   <li><strong>Expired Tokens:</strong> Tokens that have already passed their expiry date. Alert
 *       emails are sent to inform users they need to generate new tokens immediately.
 * </ul>
 *
 * <p>The scheduled task runs every 7 days (604800000 milliseconds), checking all tokens and sending
 * appropriate notifications using Thymeleaf templates. Each email includes the sensor system name,
 * username, and formatted expiry date.
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
     * Scheduled method that runs every 7 days to process all sensor authentication tokens.
     *
     * <p>This method retrieves all sensor authentication tokens in paginated batches and performs
     * two separate operations:
     *
     * <ol>
     *   <li>Identifies tokens expiring within one month and sends reminder emails
     *   <li>Identifies tokens that have already expired and sends expired token alerts
     * </ol>
     *
     * <p>Pagination is used to handle large token datasets efficiently, processing 10 tokens per
     * page.
     *
     * <p><strong>Reminder emails:</strong> Use template {@code
     * sensor-auth-token-expiry-reminder.html}
     *
     * <p><strong>Expired token alerts:</strong> Use template {@code
     * sensor-auth-token-expired-notification.html}
     *
     * @see #isExpiringWithinOneMonth(SensorAuthToken)
     * @see #isTokenExpired(SensorAuthToken)
     * @see #sendReminderEmail(SensorAuthToken)
     * @see #sendExpiredTokenEmail(SensorAuthToken)
     */
    @Scheduled(fixedRate = 604800000, initialDelay = 86400000)
    public void remindSensorAuthTokenExpiry() {
        int page = 0;
        int size = 10;
        Page<SensorAuthToken> tokenPage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            tokenPage = sensorAuthTokenService.findSensorAuthTokens(pageable);

            // Group expiring tokens by user
            Map<UnconvUser, List<SensorAuthToken>> tokensByUser =
                    tokenPage.getContent().stream()
                            .filter(this::isExpiringWithinOneMonth)
                            .collect(
                                    Collectors.groupingBy(
                                            token -> token.getSensorSystem().getUnconvUser()));

            // Send one email per user
            tokensByUser.forEach(this::sendBulkReminderEmail);

            tokenPage.getContent().stream()
                    .filter(this::isTokenExpired)
                    .forEach(this::sendExpiredTokenEmail);

            page++;
        } while (!tokenPage.isLast());
    }

    /**
     * Checks if a sensor authentication token has already expired.
     *
     * <p>This method determines whether the token's expiry date is before or equal to the current
     * time, indicating that the token is no longer valid and requires immediate attention.
     *
     * @param token the sensor authentication token to check
     * @return true if the token has expired or expires at the current time, false otherwise
     */
    private boolean isTokenExpired(SensorAuthToken token) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = token.getExpiry();
        // Check if token has already expired
        return expiry.isBefore(now) || expiry.equals(now);
    }

    /**
     * Checks if a sensor authentication token is expiring within the next month.
     *
     * <p>This method determines whether the token will expire within one month from the current
     * time but has not yet expired. Tokens that meet this criteria will trigger reminder emails to
     * users to renew their tokens before they expire.
     *
     * @param token the sensor authentication token to check
     * @return true if the token expires within one month and has not yet expired, false otherwise
     */
    private boolean isExpiringWithinOneMonth(SensorAuthToken token) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = token.getExpiry();
        // Check if token has not yet expired and will expire within one month
        return expiry.isAfter(now) && ChronoUnit.MONTHS.between(now, expiry) < 1;
    }

    /**
     * Sends a reminder email to a user about their sensor auth token expiring soon.
     *
     * <p>This method constructs and sends an HTML email using the {@code
     * sensor-auth-token-expiry-reminder.html} Thymeleaf template. The email informs the user that
     * their token will expire soon and should be renewed to avoid service interruption.
     *
     * <p>The email includes:
     *
     * <ul>
     *   <li>Username
     *   <li>Sensor system name
     *   <li>Token expiry date and time (formatted as "d MMMM yyyy, HH:mm 'UTC'")
     * </ul>
     *
     * @param token the sensor authentication token that is expiring soon
     * @see #sendExpiredTokenEmail(SensorAuthToken)
     */
    @Deprecated
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

    private void sendBulkReminderEmail(UnconvUser user, List<SensorAuthToken> tokens) {
        String email = user.getEmail();
        String subject = "⚠️ Sensor Auth Token Expiry Reminder";

        List<Map<String, String>> tokenDetails =
                tokens.stream()
                        .map(
                                token -> {
                                    Map<String, String> details = new HashMap<>();
                                    details.put(
                                            "sensorName", token.getSensorSystem().getSensorName());
                                    details.put(
                                            "expiryDate",
                                            token.getExpiry().format(EXPIRY_FORMATTER));
                                    return details;
                                })
                        .toList();

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("tokens", tokenDetails);

        String body =
                templateEngine.process("sensor-auth-token-expiry-reminder-bulk.html", context);
        emailClient.sendEmailWithHTMLContent(email, subject, body);
    }

    /**
     * Sends an alert email to a user about their expired sensor auth token.
     *
     * <p>This method constructs and sends an HTML email using the {@code
     * sensor-auth-token-expired-notification.html} Thymeleaf template. The email alerts the user
     * that their token has already expired and they must generate a new token immediately to
     * restore access.
     *
     * <p>The email includes:
     *
     * <ul>
     *   <li>Username
     *   <li>Sensor system name
     *   <li>Token expiry date and time (formatted as "d MMMM yyyy, HH:mm 'UTC'")
     *   <li>Call to action for immediate token regeneration
     * </ul>
     *
     * @param token the sensor authentication token that has already expired
     * @see #sendReminderEmail(SensorAuthToken)
     */
    private void sendExpiredTokenEmail(SensorAuthToken token) {
        UnconvUser user = token.getSensorSystem().getUnconvUser();
        String email = user.getEmail();
        String subject = "⛔ Sensor Auth Token Expired";

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("sensorName", token.getSensorSystem().getSensorName());
        context.setVariable("expiryDate", token.getExpiry().format(EXPIRY_FORMATTER));

        String body =
                templateEngine.process("sensor-auth-token-expired-notification.html", context);
        emailClient.sendEmailWithHTMLContent(email, subject, body);
    }
}
