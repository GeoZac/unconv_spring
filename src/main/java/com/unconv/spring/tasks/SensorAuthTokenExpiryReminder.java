package com.unconv.spring.tasks;

import com.unconv.spring.domain.SensorAuthToken;
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
                String username = sensorAuthToken.getSensorSystem().getUnconvUser().getUsername();
                String email =
                        sensorAuthToken
                                .getSensorSystem()
                                .getUnconvUser()
                                .getEmail(); // Assuming you have this
                String subject = "Sensor Auth Token Expiry Reminder";
                String body =
                        String.format(
                                "Hello %s,\n\nYour sensor auth token (ID: %s) is expiring on %s.\nPlease renew it in time to avoid service disruption.\n\nBest regards,\nSensor Team",
                                username, sensorAuthToken.getId(), expiryTime.toLocalDate());

                emailClient.sendEmail(email, subject, body);
            }
        }
    }
}
