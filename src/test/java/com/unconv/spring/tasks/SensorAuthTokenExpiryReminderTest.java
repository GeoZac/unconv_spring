package com.unconv.spring.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.external.EmailClient;
import com.unconv.spring.service.SensorAuthTokenService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@ExtendWith(MockitoExtension.class)
class SensorAuthTokenExpiryReminderTest {

    @Mock private SensorAuthTokenService sensorAuthTokenService;
    @Mock private EmailClient emailClient;

    @InjectMocks private SensorAuthTokenExpiryReminder reminder;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        reminder.templateEngine = templateEngine;
    }

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

        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<SensorAuthToken> mockPage = new PageImpl<>(List.of(mockToken));

        when(sensorAuthTokenService.findSensorAuthTokens(pageable)).thenReturn(mockPage);

        reminder.remindSensorAuthTokenExpiry();

        verify(emailClient)
                .sendEmailWithHTMLContent(
                        eq("john@example.com"),
                        eq("⚠️ Sensor Auth Token Expiry Reminder"),
                        argThat(body -> body.contains("john_doe") && body.contains("Mock System")));
    }

    @Test
    void shouldSendEmailForTokensAcrossMultiplePages() {
        OffsetDateTime now = OffsetDateTime.now();

        // Create 15 tokens expiring this month
        List<SensorAuthToken> allTokens =
                Instancio.ofList(SensorAuthToken.class)
                        .size(15)
                        .generate(
                                Select.field(SensorAuthToken::getExpiry),
                                gen ->
                                        gen.temporal()
                                                .offsetDateTime()
                                                .range(now.plusDays(1), now.plusDays(15)))
                        .supply(
                                Select.field(SensorAuthToken::getSensorSystem),
                                () -> {
                                    UnconvUser user =
                                            Instancio.of(UnconvUser.class)
                                                    .set(
                                                            Select.field(UnconvUser::getUsername),
                                                            "user_" + UUID.randomUUID())
                                                    .set(
                                                            Select.field(UnconvUser::getEmail),
                                                            "user"
                                                                    + UUID.randomUUID()
                                                                    + "@example.com")
                                                    .create();
                                    return Instancio.of(SensorSystem.class)
                                            .set(
                                                    Select.field(SensorSystem::getSensorName),
                                                    "System_" + UUID.randomUUID())
                                            .set(Select.field(SensorSystem::getUnconvUser), user)
                                            .create();
                                })
                        .create();

        List<SensorAuthToken> firstPageTokens = allTokens.subList(0, 10);
        List<SensorAuthToken> secondPageTokens = allTokens.subList(10, 15);

        Page<SensorAuthToken> page0 = new PageImpl<>(firstPageTokens, PageRequest.of(0, 10), 15);
        Page<SensorAuthToken> page1 = new PageImpl<>(secondPageTokens, PageRequest.of(1, 10), 15);

        // Setup stubbing for paginated calls
        when(sensorAuthTokenService.findSensorAuthTokens(PageRequest.of(0, 10))).thenReturn(page0);
        when(sensorAuthTokenService.findSensorAuthTokens(PageRequest.of(1, 10))).thenReturn(page1);

        // Act
        reminder.remindSensorAuthTokenExpiry();

        // Assert that all tokens triggered emails
        allTokens.forEach(
                token -> {
                    String expectedEmail = token.getSensorSystem().getUnconvUser().getEmail();
                    String expectedUsername = token.getSensorSystem().getUnconvUser().getUsername();
                    String expectedSystemName = token.getSensorSystem().getSensorName();

                    verify(emailClient)
                            .sendEmailWithHTMLContent(
                                    eq(expectedEmail),
                                    eq("⚠️ Sensor Auth Token Expiry Reminder"),
                                    argThat(
                                            body ->
                                                    body.contains(expectedUsername)
                                                            && body.contains(expectedSystemName)));
                });

        verify(sensorAuthTokenService, times(2)).findSensorAuthTokens(any(Pageable.class));
        verifyNoMoreInteractions(emailClient);
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

        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<SensorAuthToken> mockPage = new PageImpl<>(List.of(mockToken));

        when(sensorAuthTokenService.findSensorAuthTokens(pageable)).thenReturn(mockPage);

        reminder.remindSensorAuthTokenExpiry();

        verify(emailClient, never()).sendEmail(any(), any(), any());
    }
}
