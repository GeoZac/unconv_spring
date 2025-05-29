package com.unconv.spring.tasks;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@EnableScheduling
public class SensorAuthTokenExpiryReminder {

    @Autowired private SensorAuthTokenService sensorAuthTokenService;

    @Autowired private EmailClient emailClient;

    @Autowired private SpringTemplateEngine templateEngine;

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
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm 'UTC'", Locale.ENGLISH);
                String prettyTimeString = expiryTime.format(formatter);
                String subject = "⚠️ Sensor Auth Token Expiry Reminder";

                Context context = new Context();
                context.setVariable("username", user.getUsername());
                context.setVariable(
                        "sensorName", sensorAuthToken.getSensorSystem().getSensorName());
                context.setVariable("expiryDate", prettyTimeString);

                String body =
                        templateEngine.process("sensor-auth-token-expiry-reminder.html", context);

                emailClient.sendEmailWithHTMLContent(email, subject, body);
            }
        }
    }
}
