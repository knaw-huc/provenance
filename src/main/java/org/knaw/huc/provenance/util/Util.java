package org.knaw.huc.provenance.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Util {
    public static boolean isValidUri(String uri) {
        try {
            new URI(uri);
            return true;
        } catch (URISyntaxException use) {
            return false;
        }
    }

    public static boolean isValidTimestamp(String timestamp) {
        try {
            Instant.parse(timestamp);
            return true;
        } catch (DateTimeParseException dtpe) {
            return false;
        }
    }

    public static LocalDateTime parseIsoDate(String formattedDate) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(formattedDate);
        } catch (DateTimeParseException dte) {
            return LocalDateTime.parse(formattedDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }
}
