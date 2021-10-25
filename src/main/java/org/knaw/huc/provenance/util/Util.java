package org.knaw.huc.provenance.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Util {
    public static boolean isValidUri(String uri) {
        try {
            new URI(uri);
            return true;
        } catch (URISyntaxException use) {
            return false;
        }
    }
}
