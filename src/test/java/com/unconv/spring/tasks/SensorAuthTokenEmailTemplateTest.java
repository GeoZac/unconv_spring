package com.unconv.spring.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class SensorAuthTokenEmailTemplateTest {

    private SpringTemplateEngine templateEngine;

    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm 'UTC'", Locale.ENGLISH);

    private static final String OUTPUT_DIRECTORY = "target/email-templates";

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Test
    void testRenderExpiringTokenReminderTemplate() throws IOException {
        Context context = new Context();
        context.setVariable("username", "john_doe");
        context.setVariable("sensorName", "Temperature Sensor - Building A");
        context.setVariable(
                "expiryDate", OffsetDateTime.now().plusDays(15).format(EXPIRY_FORMATTER));

        String html = templateEngine.process("sensor-auth-token-expiry-reminder", context);

        writeTemplateToFile("sensor-auth-token-expiry-reminder.html", html);
        System.out.println(
                "\u2705 Expiring Token Reminder template rendered: "
                        + OUTPUT_DIRECTORY
                        + "/sensor-auth-token-expiry-reminder.html");
    }

    private void writeTemplateToFile(String filename, String htmlContent) throws IOException {
        Path directory = Paths.get(OUTPUT_DIRECTORY);
        Files.createDirectories(directory);

        Path filePath = directory.resolve(filename);
        Files.writeString(filePath, htmlContent);
    }
}
