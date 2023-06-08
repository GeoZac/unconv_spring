package com.unconv.spring.utils;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

public class CSVUtil {
    public static final String TYPE = "text/csv";

    public static boolean isOfCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static List<EnvironmentalReading> csvToEnvironmentalReadings(
            InputStream inputStream, SensorSystem sensorSystem) {
        try (BufferedReader fileReader =
                        new BufferedReader(
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                CSVParser csvParser =
                        new CSVParser(
                                fileReader,
                                CSVFormat.DEFAULT
                                        .withFirstRecordAsHeader()
                                        .withIgnoreHeaderCase()
                                        .withTrim()); ) {

            List<EnvironmentalReading> environmentalReadings = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                EnvironmentalReading environmentalReading =
                        new EnvironmentalReading(
                                null,
                                Double.parseDouble(csvRecord.get("temperature")),
                                Double.parseDouble(csvRecord.get("humidity")),
                                OffsetDateTime.parse(csvRecord.get("timestamp")),
                                sensorSystem);

                environmentalReadings.add(environmentalReading);
            }

            return environmentalReadings;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}
