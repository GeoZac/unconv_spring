package com.unconv.spring.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorAuthTokenExpiryReminderTest {

    @Mock private SensorAuthTokenService sensorAuthTokenService;

    @Mock private EmailClient emailClient;

    @InjectMocks private SensorAuthTokenExpiryReminder reminder;

    @Test
    void shouldSendEmailWhenTokenExpiresThisMonth() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = now.plusDays(10); // within the same month

        UnconvUser mockUser = new UnconvUser();
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@example.com");

        SensorSystem mockSystem = new SensorSystem();
        mockSystem.setSensorName("Mock System");
        mockSystem.setUnconvUser(mockUser);

        SensorAuthToken mockToken = new SensorAuthToken();
        mockToken.setId(UUID.randomUUID());
        mockToken.setExpiry(expiry);
        mockToken.setSensorSystem(mockSystem);

        when(sensorAuthTokenService.findAllSensorAuthTokens()).thenReturn(List.of(mockToken));

        reminder.remindSensorAuthTokenExpiry();

        verify(emailClient)
                .sendEmailWithHTMLContent(
                        eq("john@example.com"),
                        eq("⚠️ Sensor Auth Token Expiry Reminder"),
                        argThat(
                                body ->
                                        body.contains("john_doe")
                                                && body.contains(
                                                        mockToken
                                                                .getSensorSystem()
                                                                .getSensorName())));
    }

    @Test
    void shouldNotSendEmailWhenTokenDoesNotExpireThisMonth() {

        OffsetDateTime expiry = OffsetDateTime.now().plusMonths(2);

        UnconvUser mockUser = new UnconvUser();
        mockUser.setUsername("jane_doe");
        mockUser.setEmail("jane@example.com");

        SensorSystem mockSystem = new SensorSystem();
        mockSystem.setUnconvUser(mockUser);

        SensorAuthToken mockToken = new SensorAuthToken();
        mockToken.setId(UUID.randomUUID());
        mockToken.setExpiry(expiry);
        mockToken.setSensorSystem(mockSystem);

        when(sensorAuthTokenService.findAllSensorAuthTokens()).thenReturn(List.of(mockToken));

        reminder.remindSensorAuthTokenExpiry();

        verify(emailClient, never()).sendEmail(any(), any(), any());
    }
}
