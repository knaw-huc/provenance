package org.knaw.huc.provenance.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
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
}
