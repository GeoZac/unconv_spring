package com.unconv.spring.tasks;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SensorAuthTokenExpiryReminder {

    @Autowired private SensorAuthTokenService sensorAuthTokenService;

    @Autowired private EmailClient emailClient;

    @Scheduled(fixedRate = 604800000)
    public void remindSensorAuthTokenExpiry() {

        List<SensorAuthToken> sensorAuthTokenList =
                sensorAuthTokenService.findAllSensorAuthTokens();
        for (SensorAuthToken sensorAuthToken : sensorAuthTokenList) {
            OffsetDateTime now = OffsetDateTime.now();

            OffsetDateTime expiryTime = sensorAuthToken.getExpiry();
            long monthsDifference = ChronoUnit.MONTHS.between(expiryTime, now);

            if (Math.abs(monthsDifference) < 1) {
                UnconvUser user = sensorAuthToken.getSensorSystem().getUnconvUser();
                String email = user.getEmail();
                String subject = "⚠️ Sensor Auth Token Expiry Reminder";

                String body =
                        String.format(
                                """
                        <html>
                        <body>
                            <p>Dear <strong>%s</strong>,</p>
                            <p>This is a reminder that your sensor auth token is about to expire.</p>
                            <table style="border: 1px solid #ccc; padding: 10px;">
                                <tr><td><b>Sensor Name:</b></td><td>%s</td></tr>
                                <tr><td><b>Expiry Date:</b></td><td>%s</td></tr>
                            </table>
                            <p>Please renew it before the expiry to avoid service interruption.</p>
                            <p style="margin-top: 20px;">Best regards,<br/>Sensor Monitoring Team</p>
                        </body>
                        </html>
                        """,
                                user.getUsername(),
                                sensorAuthToken.getSensorSystem().getSensorName(),
                                expiryTime.toLocalDateTime());

                emailClient.sendEmailWithHTMLContent(email, subject, body);
            }
        }
    }
}
