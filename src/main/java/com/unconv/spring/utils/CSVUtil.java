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

/** Utility class for handling CSV files related to environmental readings. */
public class CSVUtil {

    /** The content type for CSV files. */
    public static final String TYPE = "text/csv";

    /** Private constructor to hide the implicit public one */
    private CSVUtil() {
        // Private constructor to hide the implicit public one
    }

    /**
     * Checks if the provided file is of CSV format.
     *
     * @param file the multipart file to check
     * @return true if the file is of CSV format, false otherwise
     */
    public static boolean isOfCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    /**
     * Converts a CSV file represented by an input stream into a list of {@link
     * EnvironmentalReading} objects.
     *
     * @param inputStream the input stream of the CSV file
     * @param sensorSystem the sensor system associated with the environmental readings
     * @return a list of EnvironmentalReading objects parsed from the CSV file
     * @throws RuntimeException if an error occurs during CSV parsing
     */
    public static List<EnvironmentalReading> csvToEnvironmentalReadings(
            InputStream inputStream, SensorSystem sensorSystem) {
        try (BufferedReader fileReader =
                        new BufferedReader(
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                CSVParser csvParser =
                        new CSVParser(
                                fileReader,
                                CSVFormat.Builder.create()
                                        .setHeader()
                                        .setSkipHeaderRecord(true)
                                        .setIgnoreHeaderCase(false)
                                        .setTrim(true)
                                        .build())) {

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
